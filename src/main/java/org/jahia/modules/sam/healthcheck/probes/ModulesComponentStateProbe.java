package org.jahia.modules.sam.healthcheck.probes;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleState;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Reports a Declarative Services component that never came up, in a Jahia module that Jahia reports as started.
 * Jahia sets a module to STARTED from the bundle lifecycle alone, so a module whose components are all dead is
 * still reported as started.
 *
 * <p>Two component states are reported, and each one means the component is dead:
 * <ul>
 *     <li>FAILED_ACTIVATION: the activate method or the constructor threw.</li>
 *     <li>An immediate component left in SATISFIED. SCR activates an immediate component as soon as that component
 *     is satisfied, so this state means the activation was attempted and it failed. A bind method that cannot be
 *     invoked produces this state, because SCR records no failure reason for a bind failure.</li>
 * </ul>
 *
 * <p>UNSATISFIED_REFERENCE and UNSATISFIED_CONFIGURATION are not reported. A component that waits for a service,
 * or for a configuration that the operator chose not to provide, is a supported design.
 *
 * <p>Known limit: the probe calls SCR on the request thread. A component whose activate method blocks holds its
 * component manager lock, so a health check that reads that component waits for the same lock.
 */
@Component(service = Probe.class, immediate = true)
public class ModulesComponentStateProbe implements Probe {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModulesComponentStateProbe.class);

    private static final String BLACKLIST_CONFIG_PROPERTY = "blacklist";

    /** Written by the configuration thread, read by the request threads. */
    private final List<String> blacklist = new CopyOnWriteArrayList<>();

    private volatile ServiceComponentRuntime serviceComponentRuntime;

    /**
     * SCR publishes a {@code service.changecount} service property, and it republishes that property only once no
     * component has changed state for 5 seconds. Binding the update of that property therefore gives the probe two
     * things at once: it recomputes nothing while the load balancer polls a quiet system, and it never reads a
     * component that is still activating, because during a deployment the probe serves the cached result.
     */
    private final AtomicBoolean refreshCache = new AtomicBoolean();

    private final AtomicReference<ProbeStatus> cache = new AtomicReference<>();

    @Reference(name = "scr", updated = "updatedServiceComponentRuntime")
    public void setServiceComponentRuntime(ServiceComponentRuntime serviceComponentRuntime) {
        this.serviceComponentRuntime = serviceComponentRuntime;
    }

    public void unsetServiceComponentRuntime(ServiceComponentRuntime serviceComponentRuntime) {
        this.serviceComponentRuntime = null;
    }

    protected void updatedServiceComponentRuntime(ServiceComponentRuntime serviceComponentRuntime) {
        refreshCache.set(true);
        LOGGER.debug("Declarative Services reported a change, the next health check recomputes the component states");
    }

    @Override
    public String getName() {
        return "ModulesComponentState";
    }

    @Override
    public String getDescription() {
        return "Checks if any module ships a Declarative Services component that failed to activate, while the module itself is started";
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.MEDIUM;
    }

    @Override
    public ProbeStatus getStatus() {
        ProbeStatus cached = cache.get();
        if (cached != null && !refreshCache.compareAndSet(true, false)) {
            return cached;
        }

        List<ComponentIssue> issues;
        try {
            issues = collectIssues();
        } catch (Throwable e) {
            // GqlProbe turns anything that escapes a probe into RED, and the health check servlet answers 503 on
            // RED, so an error here would take the node out of the load balancer pool.
            // Nothing is cached here, so the next health check reads the component states again.
            LOGGER.warn("Could not read the component states from the Declarative Services runtime", e);
            return new ProbeStatus("Could not read the component states from the Declarative Services runtime: "
                    + e.getMessage(), ProbeStatus.Health.YELLOW);
        }

        ProbeStatus status = toStatus(issues);
        cache.set(status);
        return status;
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        List<String> names = Collections.emptyList();
        if (config.containsKey(BLACKLIST_CONFIG_PROPERTY) && StringUtils.isNotEmpty(String.valueOf(config.get(BLACKLIST_CONFIG_PROPERTY)))) {
            names = Arrays.stream(String.valueOf(config.get(BLACKLIST_CONFIG_PROPERTY)).split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toList());
        }
        blacklist.clear();
        blacklist.addAll(names);

        // The blacklist changes which components are reported, so the cached status no longer answers the question.
        cache.set(null);
    }

    private static ProbeStatus toStatus(List<ComponentIssue> issues) {
        if (issues.isEmpty()) {
            return new ProbeStatus("All module components are active", ProbeStatus.Health.GREEN);
        }

        String details = issues.stream().map(ComponentIssue::toString).collect(Collectors.joining("\n"));
        return new ProbeStatus(String.format("Found %d component(s) that failed to activate, in modules that are started:%n%s",
                issues.size(), details), ProbeStatus.Health.YELLOW);
    }

    private List<ComponentIssue> collectIssues() {
        List<ComponentIssue> issues = new ArrayList<>();

        Bundle[] bundles = getStartedModuleBundles();
        if (bundles.length == 0) {
            return issues;
        }

        // Asking SCR for these bundles only avoids building a DTO for every component of the Karaf, Felix and Jahia
        // core bundles, which this probe never reports.
        for (ComponentDescriptionDTO description : serviceComponentRuntime.getComponentDescriptionDTOs(bundles)) {
            if (blacklist.contains(description.name)) {
                continue;
            }

            Collection<ComponentConfigurationDTO> configurations;
            try {
                configurations = serviceComponentRuntime.getComponentConfigurationDTOs(description);
            } catch (Exception e) {
                // The bundle may have gone away between the two calls. Another component still deserves a report.
                LOGGER.debug("Could not read the configurations of component {}", description.name, e);
                continue;
            }

            for (ComponentConfigurationDTO configuration : configurations) {
                String reason = getFailureReason(description, configuration);
                if (reason != null) {
                    issues.add(new ComponentIssue(description, configuration, reason));
                }
            }
        }

        return issues;
    }

    /**
     * @return the bundles of the Jahia modules that Jahia reports as started, which is the only set this probe
     *         reports on. A module in another state is already reported by the ModuleState probe.
     */
    private Bundle[] getStartedModuleBundles() {
        List<Bundle> bundles = new ArrayList<>();

        for (Bundle bundle : FrameworkService.getBundleContext().getBundles()) {
            if (!BundleUtils.isJahiaModuleBundle(bundle) || blacklist.contains(bundle.getSymbolicName())) {
                continue;
            }

            // BundleUtils.getModule creates and stores a module instance, so it runs behind isJahiaModuleBundle.
            JahiaTemplatesPackage module = BundleUtils.getModule(bundle);
            if (module != null && module.getState() != null
                    && module.getState().getState() == ModuleState.State.STARTED) {
                bundles.add(bundle);
            }
        }

        return bundles.toArray(new Bundle[0]);
    }

    /**
     * @return why this component is considered dead, or null when it is healthy or legitimately waiting
     */
    private static String getFailureReason(ComponentDescriptionDTO description, ComponentConfigurationDTO configuration) {
        if (configuration.state == ComponentConfigurationDTO.FAILED_ACTIVATION) {
            // SCR reports the whole stack trace. Only its first line, the exception and its message, belongs in a
            // probe message; the stack trace is already in the logs.
            String firstLine = StringUtils.substringBefore(StringUtils.defaultString(configuration.failure), "\n").trim();
            return StringUtils.isNotEmpty(firstLine)
                    ? "activation failed: " + StringUtils.abbreviate(firstLine, 200)
                    : "activation failed";
        }

        if (description.immediate && configuration.state == ComponentConfigurationDTO.SATISFIED) {
            return "immediate component is satisfied but was never activated, which usually means a bind method could not be invoked";
        }

        return null;
    }

    protected static class ComponentIssue {
        private final String module;
        private final String component;
        private final String reason;

        ComponentIssue(ComponentDescriptionDTO description, ComponentConfigurationDTO configuration, String reason) {
            this.module = description.bundle.symbolicName + " - " + description.bundle.version;
            this.component = description.name + "(" + configuration.id + ")";
            this.reason = reason;
        }

        public String getModule() {
            return module;
        }

        public String getComponent() {
            return component;
        }

        public String getReason() {
            return reason;
        }

        @Override
        public String toString() {
            return "module[" + module + "] component[" + component + "] " + reason;
        }
    }
}
