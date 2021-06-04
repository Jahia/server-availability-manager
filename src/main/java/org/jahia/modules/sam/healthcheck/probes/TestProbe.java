package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeStatus;
import org.osgi.service.component.annotations.Component;

import java.util.Map;

@Component(immediate = true, service = Probe.class)
public class TestProbe implements Probe {
    private ProbeStatus status = ProbeStatus.GREEN;

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
            status = ProbeStatus.valueOf((String) config.get("status"));
        }
    }
}
