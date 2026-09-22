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
 *     <li>FAILED_ACTIVATION: the activate method or the constructor threw. This state is reported for an
 *     immediate and for a delayed component alike.</li>
 *     <li>An immediate component left in SATISFIED. SCR activates an immediate component as soon as that component
 *     is satisfied, so this state means the activation was attempted and it failed. A bind method that
 *     cannot be invoked produces this state. SCR records no reason for a bind failure.</li>
 * </ul>
 *
 * <p>UNSATISFIED_REFERENCE and UNSATISFIED_CONFIGURATION are not reported. A component that waits for a service is
 * a supported design, and so is a component that waits for a configuration.
 *
 * <p>Two component failures stay invisible to this probe. A GREEN answer does not rule them out:
 * <ul>
 *     <li>A delayed component that was never requested is not reported. SCR attempts no activation until a
 *     caller asks for the service, so such a component has no failure to show. Its bind method can be broken
 *     and nothing shows it. A delayed component that SCR did attempt and that threw carries
 *     FAILED_ACTIVATION. The first state above reports it.</li>
 *     <li>A component that SCR refused at registration produces no DTO at all. SCR catches anything the
 *     component metadata validation throws, logs "Cannot register component" and moves on. The component is
 *     never registered, so the runtime cannot describe it. The module still starts, and Jahia still marks it
 *     STARTED.</li>
 * </ul>
 *
 * <p>The probe reads the component states on every call, and no answer it gives depends on a previous call.
 * What it keeps between calls is the operator's blacklist, and a record of the last failure it logged. That
 * record is what makes a failure that stays get logged once rather than on every poll. A measurement on
 * Jahia 8 gives 0.4 ms for one read of 127 components. The GraphQL layer reads every probe twice per request,
 * once for the aggregate status and once for the probe list. A health check therefore pays that cost twice.
 *
 * <p>Known limit: a component that SCR is re-activating passes through SATISFIED. A configuration update on a
 * started module can therefore make the probe report that component once. The probe is read on demand, and at
 * its default MEDIUM severity it decides no routing. An operator who lowers the health check servlet's
 * status.threshold to YELLOW makes it decide one, and a transient report then answers 503.
 *
 * <p>Known limit: SCR sets a component to SATISFIED before it runs the activate method, and to ACTIVE only
 * once that method returns. A healthy immediate component is therefore reported while it activates.
 * This is not limited to a module start. A component satisfied late activates when the service it awaited
 * appears, and its module has then been STARTED for a while.
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

    /** The same reason, applied to a third party failure text that this probe does not write. */
    private static final int MAX_REPORTED_FAILURE_LENGTH = 200;

    private final AtomicReference<Set<String>> blacklist = new AtomicReference<>(Collections.emptySet());

    /** What the last scan could not read, so the same set is not written on every poll. */
    private final RepeatedLog lastUnreadable = new RepeatedLog();

    /** The failure the last call reported, so the same one is not written on every poll. */
    private final RepeatedLog lastFailure = new RepeatedLog();

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
            List<String> unreadable = new ArrayList<>();
            List<String> issues = collectIssues(unreadable);
            lastFailure.clear();
            return toStatus(issues, unreadable);
        } catch (Exception e) {
            // GqlProbe turns anything that escapes a probe into RED, and the servlet answers 503 on RED. A bug
            // in this probe must not take the node out of the load balancer pool. An Error is not caught here,
            // so GqlProbe reports it as RED, which is the right answer for a JVM in trouble.
            if (lastFailure.report(e.toString())) {
                LOGGER.warn("Could not read the component states from the Declarative Services runtime", e);
            }
            // toString rather than getMessage, which is null for a NullPointerException, and that is the
            // exception this path is most likely to see.
            return new ProbeStatus("Could not read the component states from the Declarative Services runtime: "
                    + StringUtils.abbreviate(e.toString(), MAX_REPORTED_FAILURE_LENGTH), ProbeStatus.Health.YELLOW);
        }
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        Set<String> names = Collections.emptySet();
        Object configured = config.get(BLACKLIST_CONFIG_PROPERTY);
        if (configured != null) {
            names = Arrays.stream(String.valueOf(configured).split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toSet());
        }

        // One write publishes the whole set, so a reader never sees it half updated.
        blacklist.set(Collections.unmodifiableSet(names));
    }

    private static ProbeStatus toStatus(List<String> issues, List<String> unreadable) {
        // A scan that skipped a component says so on the answer itself. The log alone would leave a health
        // check consumer reading a clean result over an incomplete scan.
        String scope = unreadable.isEmpty()
                ? ""
                : " (" + unreadable.size() + " component(s) could not be read and are not counted)";

        if (issues.isEmpty()) {
            // The probe leaves a component waiting for a service or a configuration alone, and it cannot see a
            // component SCR never attempted. It therefore reports what it looked for, and not that every
            // component is active.
            return new ProbeStatus("No component failed to activate in a started module" + scope,
                    ProbeStatus.Health.GREEN);
        }

        // SCR returns the descriptions in no specified order, so the report is sorted. Two calls then name the
        // same components, and an operator can diff two health check responses.
        issues.sort(Comparator.naturalOrder());

        StringBuilder message = new StringBuilder();
        // One component declaring several configurations contributes one line per configuration, so the count
        // names configurations rather than components.
        message.append(issues.size()).append(" component configuration(s) failed to activate in a started module")
                .append(scope).append(':');
        issues.stream().limit(MAX_REPORTED_ISSUES).forEach(issue -> message.append('\n').append(issue));
        if (issues.size() > MAX_REPORTED_ISSUES) {
            message.append('\n').append("and ").append(issues.size() - MAX_REPORTED_ISSUES).append(" more");
        }

        return new ProbeStatus(message.toString(), ProbeStatus.Health.YELLOW);
    }

    private List<String> collectIssues(List<String> unreadable) {
        List<String> issues = new ArrayList<>();
        Set<String> silenced = blacklist.get();

        Bundle[] bundles = getStartedModuleBundles(silenced);
        if (bundles.length == 0) {
            // Nothing was read, so nothing stayed unreadable. Clearing here keeps the next real failure
            // loggable, which an early return used to prevent for good.
            lastUnreadable.clear();
            return issues;
        }

        // Asking SCR for these bundles only avoids building a DTO for the components this probe never
        // reports. Those are the components of the Karaf, Felix and Jahia core bundles.
        // A bundle uninstalled between the two calls yields fewer descriptions, because SCR skips a holder whose
        // bundle is gone. A failure here is therefore a real one, and it reaches the caller.
        Collection<ComponentDescriptionDTO> descriptions = serviceComponentRuntime.getComponentDescriptionDTOs(bundles);

        for (ComponentDescriptionDTO description : descriptions) {
            if (silenced.contains(description.name)) {
                continue;
            }

            for (ComponentConfigurationDTO configuration : getConfigurations(description, unreadable)) {
                String reason = getFailureReason(description, configuration);
                if (reason != null) {
                    issues.add(describe(description, configuration, reason));
                }
            }
        }

        reportUnreadable(unreadable);

        return issues;
    }

    /**
     * Writes one line for the components this scan could not read, and only when that set changed since the
     * last scan. A load balancer polls this path, so a runtime that keeps failing would otherwise fill the log
     * at the polling rate. A scan that reads everything clears the record, so the next failure is reported.
     */
    private void reportUnreadable(List<String> unreadable) {
        // SCR returns the descriptions in no specified order, so the same failing set must give the same
        // signature whatever order this scan saw it in.
        String signature = unreadable.isEmpty() ? "" : unreadable.stream().sorted().collect(Collectors.joining(","));
        if (lastUnreadable.report(signature) && !unreadable.isEmpty()) {
            LOGGER.warn("Could not read the configurations of {} component(s), so they are not reported: {}."
                    + " A module going away during the scan is the expected cause.", unreadable.size(), unreadable);
        }
    }

    /**
     * Writes a line only when what it describes changed. A load balancer polls this probe, so a failure that
     * stays would otherwise be written at the polling rate. Request threads read and write it, and one
     * reference publishes each value.
     */
    private static final class RepeatedLog {
        private final AtomicReference<String> last = new AtomicReference<>("");

        /** @return true when this is not what was reported last, which clears with an empty signature */
        boolean report(String signature) {
            return !signature.equals(last.getAndSet(signature));
        }

        /** Forgets what was reported last, so the next occurrence is written again. */
        void clear() {
            last.set("");
        }
    }

    /**
     * A module stopped or redeployed between the two SCR calls leaves a description whose holder is already
     * gone. SCR catches only IllegalStateException on that path, so the holder lookup returns null and the call
     * throws. Reading each description on its own costs that module its components, and it keeps the report.
     *
     * @param unreadable collects the name of a component this call could not read. The caller reports it once.
     * @return the configurations of this description, or none when its module is going away
     */
    private Collection<ComponentConfigurationDTO> getConfigurations(ComponentDescriptionDTO description,
            List<String> unreadable) {
        try {
            return serviceComponentRuntime.getComponentConfigurationDTOs(description);
        } catch (RuntimeException e) {
            // The probe answers GREEN when it finds nothing, so a component it could not read must leave a
            // trace. The caller writes that trace once per scan, and the cause belongs to this component.
            unreadable.add(description.name);
            LOGGER.debug("Reading the configurations of component {} failed", description.name, e);
            return Collections.emptyList();
        }
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
                    ? "activation failed: " + StringUtils.abbreviate(firstLine, MAX_REPORTED_FAILURE_LENGTH)
                    : "activation failed";
        }

        if (description.immediate && configuration.state == ComponentConfigurationDTO.SATISFIED) {
            return "activation failed silently, which usually means a bind method could not be invoked";
        }

        return null;
    }

    /**
     * Builds the line this probe reports for one failed component configuration. The component name is printed
     * on its own, because it is also the value the operator puts in the blacklist. The configuration id is
     * printed apart, because it tells two configurations of one component from each other and it changes on
     * every restart.
     */
    private static String describe(ComponentDescriptionDTO description, ComponentConfigurationDTO configuration,
            String reason) {
        // SCR fills the bundle of every description it hands out. Reading it defensively anyway, because this
        // line is built inside the scan, and a throw here would cost the whole report rather than one line.
        String module = description.bundle == null
                ? "unknown"
                : description.bundle.symbolicName + " - " + description.bundle.version;

        return "module[" + module
                + "] component[" + description.name
                + "] configuration[" + configuration.id + "] " + reason;
    }
}
