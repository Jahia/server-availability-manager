package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeStatus;
import org.osgi.service.component.annotations.Component;

import java.util.Map;

@Component(immediate = true, service = Probe.class)
public class TestProbe implements Probe {
    private ProbeStatus status = new ProbeStatus("Test probe status", ProbeStatus.Health.GREEN);

    @Override
    public String getName() {
        return "testProbe";
    }

    @Override
    public String getDescription() {
        return "This is a simple configurable test probe";
    }


    @Override
    public ProbeStatus getStatus() {
        return status;
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        if (config.containsKey("status")) {
            // The message is configurable so a test can make this probe carry any text through the health check
            // response, a character that takes more than one byte included, with no second bundle to install.
            // Read once, and tested as an object first: String.valueOf(null) is the four letters "null", so an
            // explicit null used to become that message.
            Object configured = config.get("message");
            String message = configured == null || String.valueOf(configured).isEmpty()
                    ? "Configured test probe status"
                    : String.valueOf(configured);
            status = new ProbeStatus(message, ProbeStatus.Health.valueOf((String) config.get("status")));
        }
    }
}
