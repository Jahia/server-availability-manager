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
public class SupportedStackJDKProbe implements Probe {

    @Override
    public ProbeStatus getStatus() {
        String vmVendor = System.getProperty("java.vm.vendor");
        Version jahiaVersion = new Version(Jahia.VERSION);
        Version jdkVersion = new Version(System.getProperty("java.version", "Unknown"));

        ProbeStatus status =  new ProbeStatus("Jahia version and your JVM version are compatible", ProbeStatus.Health.GREEN);
        if (jahiaVersion.compareTo(new Version("8.2.0.0")) < 0) {
            if (jdkVersion.compareTo(new Version("1.8")) >= 0 && jdkVersion.compareTo(new Version("11")) <= 0) {
                status = updateStatus(status, "Jahia version and JVM version are not compatible, use version 8 or 11", ProbeStatus.Health.RED);
            }
            if (!vmVendor.contains("Oracle") && !vmVendor.contains("Eclipse")) {
                status = updateStatus(status, "Current Jahia version is compatible with Eclipse Adoptium or Oracle jvm vendors", ProbeStatus.Health.YELLOW);
            }
        } else {
            if (jdkVersion.compareTo(new Version("11")) < 0) {
                status = updateStatus(status, "Jahia version and JVM version are not compatible, use version 11 or newer", ProbeStatus.Health.RED);
            }
            if(!vmVendor.contains("GraalVM") && !vmVendor.contains("Oracle") && !vmVendor.contains("Eclipse")) {
                status = updateStatus(status, "Current Jahia version is compatible with GraalVM, Oracle or Eclipse Adoptium jvm vendors", ProbeStatus.Health.YELLOW);
            }
        }
        return status;
    }

    @Override
    public String getDescription() {
        return "Checks if Jahia is running on a platform with the correct JDK version installed.";
    }

    @Override
    public String getName() {
        return "SupportedStackJDK";
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