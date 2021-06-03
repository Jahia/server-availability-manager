package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeStatus;
import org.osgi.service.component.annotations.Component;

import java.util.Map;

@Component(immediate = true, service = Probe.class)
public class MySecondProbe implements Probe {
    private ProbeStatus myStatus = ProbeStatus.RED;

    @Override
    public String getName() {
        return "mySecondProbe";
    }

    @Override
    public String getDescription() {
        return "This is my second Jahia monitoring probe";
    }

    @Override
    public ProbeStatus getStatus() {
        return myStatus;
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        if (config.containsKey("myStatus")) {
            myStatus = ProbeStatus.valueOf((String) config.get("myStatus"));
        }
    }
}
