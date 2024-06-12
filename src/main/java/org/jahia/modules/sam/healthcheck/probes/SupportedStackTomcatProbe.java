package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.commons.Version;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.settings.SettingsBean;
import org.osgi.service.component.annotations.Component;

import java.util.Map;

@Component(immediate = true, service = Probe.class)
public class SupportedStackTomcatProbe implements Probe {

    Version supportedTomcatVersion = new Version("9.0.85");
    @Override
    public String getName() {
        return "SupportedStackTomcat";
    }

    @Override
    public String getDescription() {
        return "This probe give information about Tomcat version Jahia is running on.";
    }

    @Override
    public ProbeStatus getStatus() {
        String usedVersion = SettingsBean.getInstance().getServletContext().getServerInfo();
        Version currentVersion = new Version(usedVersion.split("/")[1]);

        if (currentVersion.compareTo(supportedTomcatVersion) == 0) {
            return new ProbeStatus("Jahia is deployed on the latest supported Tomcat 9 version", ProbeStatus.Health.GREEN);
        }

        if (currentVersion.compareTo(supportedTomcatVersion) > 0) {
            return new ProbeStatus("Jahia is deployed on a Tomcat 9 version that has not been validated by Jahia", ProbeStatus.Health.YELLOW);
        }

        if (currentVersion.compareTo(supportedTomcatVersion) < 0 && currentVersion.getMajorVersion() == supportedTomcatVersion.getMajorVersion()) {
            return new ProbeStatus("Jahia is deployed on a supported Tomcat 9 version but not the latest one supported by Jahia, we encourage you to upgrade to use version", ProbeStatus.Health.YELLOW);
        }

        if (currentVersion.getMajorVersion() < supportedTomcatVersion.getMajorVersion()) {
            return new ProbeStatus("Jahia is deployed on an unsupported Tomcat version, we encourage you to upgrade to use version", ProbeStatus.Health.RED);
        }

        return new ProbeStatus("Could not complete Tomcat version check", ProbeStatus.Health.GREEN);
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.MEDIUM;
    }
}
