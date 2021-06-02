package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeStatus;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true, service = Probe.class)
public class MySecondProbe implements Probe {
    @Override
    public String getName() {
        return "mySecondProbe";
    }

    @Override
    public String getDescription() {
        return "desc";
    }

    @Override
    public ProbeStatus getStatus() {
        return ProbeStatus.RED;
    }
}
