package org.jahia.modules.sam.healthcheck.probes;

import org.graalvm.polyglot.Context;
import org.jahia.bin.Jahia;
import org.jahia.commons.Version;
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
public class SupportedStackJVMProbe implements Probe {

    @Override
    public ProbeStatus getStatus() {
        String vmVendor = System.getProperty("java.vm.vendor", "Unknown");
        Version jahiaVersion = new Version(Jahia.VERSION);
        Version jvmVersion = new Version(System.getProperty("java.version", "Unknown"));

        ProbeStatus status =  new ProbeStatus(String.format("Jahia version and your JVM version are compatible (detected %s - JVM: %s)", vmVendor, jvmVersion), ProbeStatus.Health.GREEN);
        if (jvmVersion.compareTo(new Version("11")) < 0) {
            status = updateStatus(status, String.format("Unsuported JVM version, use version 11 or newer (detected: %s)", jvmVersion), ProbeStatus.Health.RED);
        }
        if(!vmVendor.contains("GraalVM") && !vmVendor.contains("Oracle") && !vmVendor.contains("Eclipse")) {
            status = updateStatus(status, String.format("Unsupported JVM vendor, use Eclipse Adoptium or Oracle (detected: %s)", vmVendor), ProbeStatus.Health.YELLOW);
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

    private ProbeStatus updateStatus(ProbeStatus status, String message, ProbeStatus.Health health) {
        if (status.getHealth() == ProbeStatus.Health.GREEN) {
            status.setMessage(message);
            status.setHealth(health);
        } else {
            if(health == ProbeStatus.Health.RED) {
                status.setHealth(health);
            }
            status.setMessage(status.getMessage() + " - " + message);
        }
        return status;
    }
}