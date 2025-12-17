package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.modules.sam.core.ProbeStatusUtils;
import org.jahia.osgi.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;

@Component(service = Probe.class, immediate = true)
public class SupportedStackJSModulesProbe implements Probe {

    @Override
    public ProbeStatus getStatus() {
        Bundle javaScriptModulesEngineBundle = BundleUtils.getBundleBySymbolicName("javascript-modules-engine", null);
        boolean isJavaScriptModulesEngineStarted = javaScriptModulesEngineBundle != null && javaScriptModulesEngineBundle.getState() == Bundle.ACTIVE;

        ProbeStatus status = new ProbeStatus("No issues to report", ProbeStatus.Health.GREEN);
        if (!isJavaScriptModulesEngineStarted) {
            ProbeStatusUtils.aggregateStatus(status, "The environment is not running JS modules (javascript-modules-engine stopped or not present)", ProbeStatus.Health.GREEN);
        }
        return status;
    }

    @Override
    public String getDescription() {
        return "Validates the capacity of the Jahia environment to run JS modules.";
    }

    @Override
    public String getName() {
        return "SupportedStackJSModules";
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.MEDIUM;
    }
}
