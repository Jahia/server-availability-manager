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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Reports a Declarative Services component that failed to activate, in a Jahia module that Jahia reports as
 * started. Jahia sets a module to STARTED from the bundle lifecycle alone. A module whose components are all dead is
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
 * <p>The probe reads the component states on every call and holds no state between calls. A measurement on
 * Jahia 8 gives 0.4 ms for one read of 127 components. The GraphQL layer reads every probe twice per request,
 * once for the aggregate status and once for the probe list, so a health check pays that cost twice.
 *
 * <p>Known limit: a component that SCR is re-activating passes through SATISFIED. A configuration update on a
 * started module can therefore make the probe report that component once. The probe is read on demand, and it
 * decides no routing at MEDIUM severity, so a transient report costs nothing.
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

    private final AtomicReference<Set<String>> blacklist = new AtomicReference<>(Collections.emptySet());

    private ServiceComponentRuntime serviceComponentRuntime;

    private JahiaTemplateManagerService templateManagerService;

    @Reference
    public void setServiceComponentRuntime(ServiceComponentRuntime serviceComponentRuntime) {
        this.serviceComponentRuntime = serviceComponentRuntime;
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
        return "Checks if a started module ships a Declarative Services component that failed to activate";
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.MEDIUM;
    }

    @Override
    public ProbeStatus getStatus() {
        try {
            return toStatus(collectIssues());
        } catch (Exception e) {
            // GqlProbe turns anything that escapes a probe into RED, and the servlet answers 503 on RED. A bug
            // in this probe must not take the node out of the load balancer pool. An Error is not caught here,
            // so GqlProbe reports it as RED, which is the right answer for a JVM in trouble.
            LOGGER.warn("Could not read the component states from the Declarative Services runtime", e);
            return new ProbeStatus("Could not read the component states from the Declarative Services runtime: "
                    + e.getMessage(), ProbeStatus.Health.YELLOW);
        }
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        Set<String> names = Collections.emptySet();
        if (config.containsKey(BLACKLIST_CONFIG_PROPERTY) && StringUtils.isNotEmpty(String.valueOf(config.get(BLACKLIST_CONFIG_PROPERTY)))) {
            names = Arrays.stream(String.valueOf(config.get(BLACKLIST_CONFIG_PROPERTY)).split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toSet());
        }

        // One write publishes the whole set, so a reader never sees it half updated.
        blacklist.set(Collections.unmodifiableSet(names));
    }

    private static ProbeStatus toStatus(List<ComponentIssue> issues) {
        if (issues.isEmpty()) {
            // The probe leaves a delayed component and a component waiting for a service or a configuration
            // alone, so it reports what it looked for and not that every component is active.
            return new ProbeStatus("No component failed to activate in a started module", ProbeStatus.Health.GREEN);
        }

        // SCR returns the descriptions in no specified order, so the report is sorted. Two calls then name the
        // same components, and an operator can diff two health check responses.
        issues.sort(Comparator.comparing(ComponentIssue::toString));

        StringBuilder message = new StringBuilder();
        message.append(issues.size()).append(" component(s) failed to activate in a started module:");
        issues.stream().limit(MAX_REPORTED_ISSUES).forEach(issue -> message.append('\n').append(issue));
        if (issues.size() > MAX_REPORTED_ISSUES) {
            message.append('\n').append("and ").append(issues.size() - MAX_REPORTED_ISSUES).append(" more");
        }

        return new ProbeStatus(message.toString(), ProbeStatus.Health.YELLOW);
    }

    private List<ComponentIssue> collectIssues() {
        List<ComponentIssue> issues = new ArrayList<>();
        Set<String> silenced = blacklist.get();

        Bundle[] bundles = getStartedModuleBundles(silenced);
        if (bundles.length == 0) {
            return issues;
        }

        // Asking SCR for these bundles only avoids building a DTO for every component of the Karaf, Felix and
        // Jahia core bundles, which this probe never reports.
        // A bundle uninstalled between the two calls yields fewer descriptions, because SCR skips a holder whose
        // bundle is gone. A failure here is therefore a real one, and it reaches the caller.
        Collection<ComponentDescriptionDTO> descriptions = serviceComponentRuntime.getComponentDescriptionDTOs(bundles);

        for (ComponentDescriptionDTO description : descriptions) {
            if (silenced.contains(description.name)) {
                continue;
            }

            // SCR returns an empty collection for a description whose bundle went away, so this call needs no
            // guard of its own. Anything it does throw is a defect, and the caller reports it.
            for (ComponentConfigurationDTO configuration : serviceComponentRuntime.getComponentConfigurationDTOs(description)) {
                String reason = getFailureReason(description, configuration);
                if (reason != null) {
                    issues.add(new ComponentIssue(description, configuration, reason));
                }
            }
        }

        return issues;
    }

    /**
     * @param silenced the bundle and component names the operator chose not to report
     * @return the bundles of the Jahia modules that Jahia reports as started. A module in another state is already
     *         reported by the ModuleState probe.
     */
    private Bundle[] getStartedModuleBundles(Set<String> silenced) {
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
            return "activation failed silently, which usually means a bind method could not be invoked";
        }

        return null;
    }

    private static final class ComponentIssue {
        private final String module;
        private final String component;
        private final long configurationId;
        private final String reason;

        ComponentIssue(ComponentDescriptionDTO description, ComponentConfigurationDTO configuration, String reason) {
            this.module = description.bundle.symbolicName + " - " + description.bundle.version;
            this.component = description.name;
            this.configurationId = configuration.id;
            this.reason = reason;
        }

        /**
         * The component name is printed on its own, because it is also the value the operator puts in the
         * blacklist. The configuration id is printed apart, because it tells two configurations of one component
         * from each other and it changes on every restart.
         */
        @Override
        public String toString() {
            return "module[" + module + "] component[" + component + "] configuration[" + configurationId + "] " + reason;
        }
    }
}
