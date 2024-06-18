package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.commons.Version;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.settings.SettingsBean;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

@Component(immediate = true, service = Probe.class)
public class SupportedStackTomcatProbe implements Probe {

    private static final Logger logger = LoggerFactory.getLogger(SupportedStackTomcatProbe.class);

    private int maxPatchVersionDiff = 25;

    @Override
    public String getName() {
        return "SupportedStackTomcat";
    }

    @Override
    public String getDescription() {
        return "This probe gives information about Tomcat version Jahia is running on.";
    }

    @Override
    public ProbeStatus getStatus() {
        Version supportedTomcatVersion;

        try {
            supportedTomcatVersion = new Version(getProperties().getProperty("jahia.tomcat.version"));
        } catch (IOException e) {
            logger.error("Encountered an issue reading current Tomcat version", e);
            return new ProbeStatus("Could not complete Tomcat version check: Encountered an issue reading current Tomcat version", ProbeStatus.Health.GREEN);
        }

        String usedVersion = SettingsBean.getInstance().getServletContext().getServerInfo();
        Version currentVersion = new Version(usedVersion.split("/")[1]);

        if (currentVersion.getMajorVersion() != supportedTomcatVersion.getMajorVersion()) {
            return new ProbeStatus("Jahia is deployed on an unsupported Tomcat version, we encourage you to upgrade to use version", ProbeStatus.Health.RED);
        }

        if (currentVersion.getMinorVersion() != supportedTomcatVersion.getMinorVersion()) {
            return new ProbeStatus(String.format("Jahia is deployed on a Tomcat %d version that has not been validated by Jahia", supportedTomcatVersion.getMajorVersion()), ProbeStatus.Health.YELLOW);
        }

        int patchVersionDiff = currentVersion.getServicePackVersion() - supportedTomcatVersion.getServicePackVersion();
        if (patchVersionDiff < 0) {
            return new ProbeStatus(String.format("Jahia is deployed on a supported Tomcat %d version but not the latest one supported by Jahia, we encourage you to upgrade to use version", supportedTomcatVersion.getMajorVersion()), ProbeStatus.Health.YELLOW);
        }

        if (patchVersionDiff > maxPatchVersionDiff) {
            return new ProbeStatus(String.format("Jahia is deployed on a Tomcat %d version that has not been validated by Jahia", supportedTomcatVersion.getMajorVersion()), ProbeStatus.Health.YELLOW);
        }

        return new ProbeStatus(String.format("Jahia is deployed on the latest supported Tomcat %d version", supportedTomcatVersion.getMajorVersion()), ProbeStatus.Health.GREEN);
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.MEDIUM;
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        if (config.containsKey("maxPatchVersionDiff")) {
            maxPatchVersionDiff = Integer.parseInt((String) config.get("maxPatchVersionDiff"));
        }
    }

    private Properties getProperties() throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("maven.properties");
        Properties p = new Properties();
        p.load(is);
        return p;
    }
}
