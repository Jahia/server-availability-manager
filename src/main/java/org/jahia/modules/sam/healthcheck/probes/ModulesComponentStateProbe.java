package org.jahia.modules.sam.healthcheck.probes;

import org.apache.commons.lang.StringUtils;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Reports Declarative Services components that a Jahia module ships but that never came up, while the module itself
 * is started. Jahia marks a module STARTED from the bundle lifecycle alone, so a module whose components are all
 * dead still shows up as green everywhere else.
 *
 * <p>Two symptoms are reported, and both mean the component is dead:
 * <ul>
 *     <li>{@code FAILED_ACTIVATION} — the activate method or the constructor threw.</li>
 *     <li>an immediate component left in {@code SATISFIED} — SCR must activate an immediate component as soon as it
 *     is satisfied, so this is the signature of an activation that was attempted and failed. A bind method that
 *     cannot be invoked lands here, because SCR records no failure reason for it and logs the cause at DEBUG.</li>
 * </ul>
 *
 * <p>{@code UNSATISFIED_REFERENCE} and {@code UNSATISFIED_CONFIGURATION} are deliberately NOT reported: a component
 * that waits for a service or for a configuration that the operator chose not to provide is a supported design.
 */
@Component(service = Probe.class, immediate = true)
public class ModulesComponentStateProbe implements Probe {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModulesComponentStateProbe.class);

    private static final String BLACKLIST_CONFIG_PROPERTY = "blacklist";

    private List<String> blacklist = Collections.emptyList();

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
        } catch (Exception e) {
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
        if (config.containsKey(BLACKLIST_CONFIG_PROPERTY) && StringUtils.isNotEmpty(String.valueOf(config.get(BLACKLIST_CONFIG_PROPERTY)))) {
            blacklist = Arrays.stream(String.valueOf(config.get(BLACKLIST_CONFIG_PROPERTY)).split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toList());
        } else {
            blacklist = Collections.emptyList();
        }

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

        for (ComponentDescriptionDTO description : serviceComponentRuntime.getComponentDescriptionDTOs()) {
            if (!isComponentToCheck(description)) {
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
                    issues.add(new ComponentIssue(description, reason));
                }
            }
        }

        return issues;
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

    private boolean isComponentToCheck(ComponentDescriptionDTO description) {
        if (blacklist.contains(description.bundle.symbolicName) || blacklist.contains(description.name)) {
            return false;
        }

        Bundle bundle = FrameworkService.getBundleContext().getBundle(description.bundle.id);
        return bundle != null && BundleUtils.isJahiaModuleBundle(bundle);
    }

    protected static class ComponentIssue {
        private final String module;
        private final String component;
        private final String reason;

        ComponentIssue(ComponentDescriptionDTO description, String reason) {
            this.module = description.bundle.symbolicName + " - " + description.bundle.version;
            this.component = description.name;
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
