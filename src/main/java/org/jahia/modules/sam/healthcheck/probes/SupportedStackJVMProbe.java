package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.commons.Version;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.modules.sam.core.ProbeStatusUtils;
import org.osgi.service.component.annotations.Component;

@Component(service = Probe.class, immediate = true)
public class SupportedStackJVMProbe implements Probe {

    @Override
    public ProbeStatus getStatus() {
        String vmVendor = System.getProperty("java.vm.vendor", "Unknown");
        Version jvmVersion = new Version(System.getProperty("java.version", "Unknown"));

        ProbeStatus status = new ProbeStatus(String.format("Jahia version and your JVM version are compatible (detected %s - JVM: %s)", vmVendor, jvmVersion), ProbeStatus.Health.GREEN);
        if (jvmVersion.compareTo(new Version("11")) < 0) {
            ProbeStatusUtils.aggregateStatus(status, String.format("Unsupported JVM version, use version 11 or newer (detected: %s)", jvmVersion), ProbeStatus.Health.RED);
        }
        if(!vmVendor.contains("GraalVM") && !vmVendor.contains("Oracle") && !vmVendor.contains("Eclipse")) {
            ProbeStatusUtils.aggregateStatus(status, String.format("Unsupported JVM vendor, use Eclipse Adoptium, GraalVM or Oracle (detected: %s)", vmVendor), ProbeStatus.Health.YELLOW);
        }
        return status;
    }

    @Override
    public String getDescription() {
        return "Checks if Jahia is running on a platform with a supported JVM version installed.";
    }

    @Override
    public String getName() {
        return "SupportedStackJVM";
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.MEDIUM;
    }
}
