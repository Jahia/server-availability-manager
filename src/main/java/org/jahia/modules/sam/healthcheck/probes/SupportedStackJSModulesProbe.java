package org.jahia.modules.sam.healthcheck.probes;

import org.graalvm.polyglot.Context;
import org.jahia.bin.Jahia;
import org.jahia.commons.Version;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.modules.sam.core.ProbeStatusUtils;
import org.jahia.osgi.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@Component(service = Probe.class, immediate = true)
public class SupportedStackJSModulesProbe implements Probe {

    @Override
    public ProbeStatus getStatus() {
        Bundle npmModulesEngineBundle = BundleUtils.getBundleBySymbolicName("npm-modules-engine", null);
        boolean isNpmModulesEngineStarted = npmModulesEngineBundle != null && npmModulesEngineBundle.getState() == Bundle.ACTIVE;

        String vmVendor = System.getProperty("java.vm.vendor", "Unknown");
        Version jvmVersion = new Version(System.getProperty("java.version", "Unknown"));

        ProbeStatusUtils probStatusUtils = ProbeStatusUtils.getInstance();

        // This probe is only relevant for Jahia 8.2.0.0+ in which npm-modules-engine is available. 
        // Not testing Jahia version since it is not to be backported to older versions of SAM.
        ProbeStatus status = new ProbeStatus("No issues to report", ProbeStatus.Health.GREEN);
        if (!isNpmModulesEngineStarted) {
            status = probStatusUtils.aggregateStatus(status, "The environment is not running JS modules (npm-modules-engine stopped or not present)", ProbeStatus.Health.GREEN);
        } else {
            if (!vmVendor.contains("GraalVM")) {
                status = probStatusUtils.aggregateStatus(status, String.format("GraalVM not detected on the environment (detected vendor: %s), after switching to GraalVM make sure to enable the Javascript extension", vmVendor), ProbeStatus.Health.RED);
            }
            if (jvmVersion.compareTo(new Version("17")) <= 0) {
                status = probStatusUtils.aggregateStatus(status, String.format("GraalVM with JVM version 17 or newer required (detected: %s)", jvmVersion), ProbeStatus.Health.RED);
            }            
            if (vmVendor.contains("GraalVM") && !isJavaScriptModuleInstalled()) {
                status = probStatusUtils.aggregateStatus(status, "GraalVM is detected but the JavaScript extension is not installed", ProbeStatus.Health.RED);
            }
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

    private boolean isJavaScriptModuleInstalled() {
        try (Context context = Context.create()) {
            return context.getEngine().getLanguages().containsKey("js");

        }
    }
}