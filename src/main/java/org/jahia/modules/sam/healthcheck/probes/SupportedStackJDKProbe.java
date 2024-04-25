package org.jahia.modules.sam.healthcheck.probes;

import org.graalvm.polyglot.Context;
import org.jahia.bin.Jahia;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.osgi.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@Component(service = Probe.class, immediate = true)
public class SupportedStackJDKProbe implements Probe {

    private static final Logger LOGGER = LoggerFactory.getLogger(SupportedStackJDKProbe.class);

    @Override
    public ProbeStatus getStatus() {
        String jdkVersion = System.getProperty("java.version");
        String jahiaVersion = Jahia.VERSION;
        int[] jahiaDigits = Arrays.stream(jahiaVersion.split("\\.")).map(Integer::valueOf).mapToInt(i -> i).toArray();
        int[] jdkDigits = Arrays.stream(jdkVersion.split("\\.")).map(Integer::valueOf).mapToInt(i -> i).toArray();
        Bundle npmModulesEngineBundle = BundleUtils.getBundleBySymbolicName("npm-modules-engine", null);
        boolean isNpmModulesEngineStarted = npmModulesEngineBundle != null && npmModulesEngineBundle.getState() == Bundle.ACTIVE;
        String vmVendor = System.getProperty("java.vm.vendor");
        if (jahiaDigits[0] < 8 || (jahiaDigits[0] == 8 && jahiaDigits[1] < 2)) {
            if (jdkDigits[0] != 8 && jdkDigits[0] != 11) {
                return new ProbeStatus("Jahia and Jdk are not compatible, use v8 or v11", ProbeStatus.Health.RED);
            }
            if (!vmVendor.contains("Oracle") && !vmVendor.contains("Eclipse")) {
                return new ProbeStatus("Jahia is compatible with Eclipse Adoptium or Oracle jvm vendors", ProbeStatus.Health.YELLOW);
            }
        } else {
            if (jdkDigits[0] < 11) {
                return new ProbeStatus("Jahia and Jdk are not compatible, use v11 or newer", ProbeStatus.Health.RED);
            }

            if(vmVendor.contains("GraalVM") && !isJavaScriptModuleInstalled()) {
                return new ProbeStatus("GraalVM is used but JavaScript module is not installed", ProbeStatus.Health.RED);
            }

            if(!vmVendor.contains("GraalVM") && !vmVendor.contains("Oracle") && !vmVendor.contains("Eclipse")) {
                return new ProbeStatus("Jahia is compatible with GraalVM, Oracle or Eclipse Adoptium jvm vendors", ProbeStatus.Health.YELLOW);
            }

            if(isNpmModulesEngineStarted && !vmVendor.contains("GraalVM")) {
                return new ProbeStatus("Npm modules engine needs vm vendor to be GraalVM and jdk version to be 17", ProbeStatus.Health.YELLOW);
            }

            if (isNpmModulesEngineStarted && jdkDigits[0] == 11) {
                return new ProbeStatus("Npm modules engine is started with JDK 11, please use JDK 17", ProbeStatus.Health.YELLOW);
            }
        }
        return new ProbeStatus("Jahia and your JDK are compatible", ProbeStatus.Health.GREEN);
    }

    @Override
    public String getDescription() {
        return "Check if your Jahia version is compatible with your JDK version and GraalVM is installed if needed.";
    }

    @Override
    public String getName() {
        return "SupportedStackJDK";
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