package org.jahia.modules.sam.test;

import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.osgi.service.component.annotations.Component;

import java.util.Map;

@Component(service = Probe.class, immediate = true)
public class DummyProbe implements Probe {

    public String getName() {
        return "DummyState";
    }

    @Override
    public String getDescription() {
        return "Probe using for checking the registering of probe by another bundle";
    }

    @Override
    public ProbeStatus getStatus() {
        return new ProbeStatus("Always return green", ProbeStatus.Health.GREEN);
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.HIGH;
    }

    @Override
    public void setConfig(Map<String, Object> config) {
    }
}
