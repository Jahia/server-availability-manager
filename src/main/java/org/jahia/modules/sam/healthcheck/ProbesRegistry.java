package org.jahia.modules.sam.healthcheck;

import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component(immediate = true, service = ProbesRegistry.class)
public class ProbesRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ProbesRegistry.class);

    private Map<String, Object> config;
    private final Collection<Probe> probes = new ArrayList<>();

    @Activate
    public void activate(Map<String, Object> props) {
        this.config = props;
        for (Probe probe : probes) {
            activateProbe(probe);
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void addProbe(Probe probe) {
        probes.add(probe);
        activateProbe(probe);
    }

    public void removeProbe(Probe probe) {
        probes.remove(probe);
    }

    private void activateProbe(Probe probe) {
        if (config != null) {
            probe.setConfig(getProbeConfig(probe.getName()));
        }
    }

    public ProbeSeverity getProbeSeverity(Probe probe) {
        String key = "probes." + probe.getName() + ".severity";
        if (config.containsKey(key)) {
            try {
                return ProbeSeverity.valueOf((String) config.get(key));
            } catch (IllegalArgumentException e) {
                logger.error("Cannot parse severity", e);
            }
        }
        return probe.getDefaultSeverity();
    }

    public Map<String, Object> getProbeConfig(String name) {
        String configPrefix = "probes." + name + ".";
        return config.keySet().stream()
                .filter(k -> k.startsWith(configPrefix))
                .map(k -> k.substring(configPrefix.length()))
                .collect(Collectors.toMap(k -> k, k -> config.get(configPrefix + k)));
    }

    public Collection<Probe> getProbes() {
        return probes;
    }
}
