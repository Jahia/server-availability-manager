package org.jahia.modules.sam.healthcheck.probes;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.ModuleState;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.services.templates.JahiaTemplateManagerService;
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
 * Reports a Declarative Services component that never came up, in a Jahia module that Jahia reports as started.
 * Jahia sets a module to STARTED from the bundle lifecycle alone. A module whose components are all dead is
 * therefore still reported as started.
 *
 * <p>Two component states are reported, and each one means the component is dead:
 * <ul>
 *     <li>FAILED_ACTIVATION: the activate method or the constructor threw.</li>
 *     <li>An immediate component left in SATISFIED. SCR activates an immediate component as soon as that component
 *     is satisfied, so this state means the activation was attempted and it failed. A bind method that cannot be
 *     invoked produces this state, because SCR records no failure reason for a bind failure.</li>
 * </ul>
 *
 * <p>UNSATISFIED_REFERENCE and UNSATISFIED_CONFIGURATION are not reported. A component that waits for a service is
 * a supported design, and so is a component that waits for a configuration.
 *
 * <p>Known limit: the probe calls SCR on the request thread. A component whose activate method blocks holds its
 * component manager lock, so a health check that reads that component waits for the same lock.
 */
@Component(service = Probe.class, immediate = true)
public class ModulesComponentStateProbe implements Probe {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModulesComponentStateProbe.class);

    private static final String BLACKLIST_CONFIG_PROPERTY = "blacklist";

    /** A health check answers a load balancer, so the message stays bounded. */
    private static final int MAX_REPORTED_ISSUES = 10;

    /**
     * The reported set also depends on the Jahia module state, and a module can leave STARTED with no component
     * state change. This delay bounds how long the probe serves a status that no signal invalidated.
     */
    private static final long CACHE_TTL_MS = 30000L;

    private final AtomicReference<List<String>> blacklist = new AtomicReference<>(Collections.emptyList());

    private volatile ServiceComponentRuntime serviceComponentRuntime;

    private volatile JahiaTemplateManagerService templateManagerService;

    /**
     * SCR publishes a {@code service.changecount} service property. SCR republishes that property only once no
     * component has changed state for 5 seconds, so this flag also tells the probe that the system settled.
     */
    private final AtomicBoolean refreshCache = new AtomicBoolean();

    private final AtomicReference<ProbeStatus> cache = new AtomicReference<>();

    private volatile long cachedAt;

    @Reference(name = "scr", updated = "updatedServiceComponentRuntime")
    public void setServiceComponentRuntime(ServiceComponentRuntime serviceComponentRuntime) {
        this.serviceComponentRuntime = serviceComponentRuntime;
    }

    public void unsetServiceComponentRuntime(ServiceComponentRuntime serviceComponentRuntime) {
        this.serviceComponentRuntime = null;
    }

    protected void updatedServiceComponentRuntime(ServiceComponentRuntime serviceComponentRuntime) {
        refreshCache.set(true);
        LOGGER.debug("Declarative Services reported a change, the next health check recomputes");
    }

    @Reference
    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
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
        boolean expired = System.currentTimeMillis() - cachedAt >= CACHE_TTL_MS;
        if (cached != null && !expired && !refreshCache.compareAndSet(true, false)) {
            return cached;
        }

        List<ComponentIssue> issues;
        try {
            issues = collectIssues();
        } catch (Throwable e) {
            // GqlProbe turns anything that escapes a probe into RED. The health check servlet answers 503 on RED,
            // which takes the node out of the load balancer pool.
            LOGGER.warn("Could not read the component states from the Declarative Services runtime", e);
            // The refresh signal was consumed above, so it is raised again. Without it, the next health check
            // would serve the status cached before this failure.
            refreshCache.set(true);
            return new ProbeStatus("Could not read the component states from the Declarative Services runtime: "
                    + e.getMessage(), ProbeStatus.Health.YELLOW);
        }

        ProbeStatus status = toStatus(issues);
        cachedAt = System.currentTimeMillis();
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

        // One write publishes the whole list, so a reader never sees it half updated.
        blacklist.set(Collections.unmodifiableList(names));

        // The blacklist changes which components are reported, so the cached status is dropped.
        cachedAt = 0L;
        cache.set(null);
    }

    private static ProbeStatus toStatus(List<ComponentIssue> issues) {
        if (issues.isEmpty()) {
            return new ProbeStatus("All module components are active", ProbeStatus.Health.GREEN);
        }

        String details = issues.stream()
                .limit(MAX_REPORTED_ISSUES)
                .map(ComponentIssue::toString)
                .collect(Collectors.joining("\n"));
        if (issues.size() > MAX_REPORTED_ISSUES) {
            details = details + String.format("%nand %d more", issues.size() - MAX_REPORTED_ISSUES);
        }

        return new ProbeStatus(String.format("Found %d component(s) that failed to activate, in modules that are started:%n%s",
                issues.size(), details), ProbeStatus.Health.YELLOW);
    }

    private List<ComponentIssue> collectIssues() {
        List<ComponentIssue> issues = new ArrayList<>();
        List<String> silenced = blacklist.get();

        Bundle[] bundles = getStartedModuleBundles(silenced);
        if (bundles.length == 0) {
            return issues;
        }

        // Asking SCR for these bundles only avoids building a DTO for every component of the Karaf, Felix and
        // Jahia core bundles, which this probe never reports.
        Collection<ComponentDescriptionDTO> descriptions = serviceComponentRuntime.getComponentDescriptionDTOs(bundles);
        int read = 0;
        int failed = 0;

        for (ComponentDescriptionDTO description : descriptions) {
            if (silenced.contains(description.name)) {
                continue;
            }

            Collection<ComponentConfigurationDTO> configurations;
            try {
                configurations = serviceComponentRuntime.getComponentConfigurationDTOs(description);
                read++;
            } catch (Exception e) {
                // The bundle may have gone away between the two calls. Another component still deserves a report.
                LOGGER.debug("Could not read the configurations of component {}", description.name, e);
                failed++;
                continue;
            }

            for (ComponentConfigurationDTO configuration : configurations) {
                String reason = getFailureReason(description, configuration);
                if (reason != null) {
                    issues.add(new ComponentIssue(description, configuration, reason));
                }
            }
        }

        if (read == 0 && failed > 0) {
            // Every component failed to read, so an empty list would report a healthy instance.
            throw new IllegalStateException("None of the " + failed + " component(s) could be read");
        }

        return issues;
    }

    /**
     * @param silenced the bundle and component names the operator chose not to report
     * @return the bundles of the Jahia modules that Jahia reports as started. A module in another state is already
     *         reported by the ModuleState probe.
     */
    private Bundle[] getStartedModuleBundles(List<String> silenced) {
        List<Bundle> bundles = new ArrayList<>();

        for (Map.Entry<Bundle, ModuleState> entry : templateManagerService.getModuleStates().entrySet()) {
            Bundle bundle = entry.getKey();
            if (silenced.contains(bundle.getSymbolicName())) {
                continue;
            }
            if (entry.getValue() != null && entry.getValue().getState() == ModuleState.State.STARTED) {
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
            // SCR reports the whole stack trace. Only its first line belongs in a probe message, because the
            // stack trace is already in the logs.
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

    private static final class ComponentIssue {
        private final String module;
        private final String component;
        private final String reason;

        ComponentIssue(ComponentDescriptionDTO description, ComponentConfigurationDTO configuration, String reason) {
            this.module = description.bundle.symbolicName + " - " + description.bundle.version;
            this.component = description.name + "(" + configuration.id + ")";
            this.reason = reason;
        }

        @Override
        public String toString() {
            return "module[" + module + "] component[" + component + "] " + reason;
        }
    }
}
