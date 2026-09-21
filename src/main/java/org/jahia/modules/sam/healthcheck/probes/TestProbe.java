package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component(immediate = true, service = Probe.class)
public class TestProbe extends AbstractProbe {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestProbe.class);
    private static final String STATUS_CONFIG_PROPERTY = "status";
    private static final ProbeStatus DEFAULT_STATUS = new ProbeStatus("Test probe status", ProbeStatus.Health.GREEN);

    private volatile ProbeStatus status = DEFAULT_STATUS;

    public TestProbe() {
        super("testProbe", "This is a simple configurable test probe", ProbeSeverity.IGNORED);
    }

    @Override
    public ProbeStatus getStatus() {
        return status;
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        // A health this probe does not know must not make it reject its whole configuration.
        // A property the operator removed returns it to the default.
        Object configured = config.get(STATUS_CONFIG_PROPERTY);
        if (configured == null || String.valueOf(configured).isEmpty()) {
            status = DEFAULT_STATUS;
            return;
        }

        try {
            status = new ProbeStatus("Configured test probe status",
                    ProbeStatus.Health.valueOf(String.valueOf(configured)));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("The {} property of this probe names no known health, so the default is used: {}",
                    STATUS_CONFIG_PROPERTY, configured);
            status = DEFAULT_STATUS;
        }
    }
}
