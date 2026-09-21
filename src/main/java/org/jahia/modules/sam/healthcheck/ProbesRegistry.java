package org.jahia.modules.sam.healthcheck;

import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component(immediate = true, service = ProbesRegistry.class)
public class ProbesRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ProbesRegistry.class);

    private final AtomicReference<Map<String, Object>> config = new AtomicReference<>();
    private final Collection<Probe> probes = new CopyOnWriteArrayList<>();

    /**
     * Serves as both the activation and the modified method: re-reading the properties and re-applying
     * them to every registered probe is exactly the work a configuration update needs.
     */
    @Activate
    @Modified
    public void activate(Map<String, Object> props) {
        config.set(props);
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
        if (config.get() != null) {
            probe.setConfig(getProbeConfig(probe.getName()));
        }
    }

    public ProbeSeverity getProbeSeverity(Probe probe) {
        Map<String, Object> currentConfig = config.get();
        String key = "probes." + probe.getName() + ".severity";
        if (currentConfig.containsKey(key)) {
            try {
                return ProbeSeverity.valueOf((String) currentConfig.get(key));
            } catch (IllegalArgumentException e) {
                logger.error("Cannot parse severity", e);
            }
        }
        return probe.getDefaultSeverity();
    }

    public Map<String, Object> getProbeConfig(String name) {
        Map<String, Object> currentConfig = config.get();
        String configPrefix = "probes." + name + ".";
        return currentConfig.keySet().stream()
                .filter(k -> k.startsWith(configPrefix))
                .collect(Collectors.toMap(k -> k.substring(configPrefix.length()), currentConfig::get));
    }

    public Collection<Probe> getProbes() {
        return probes;
    }
}
