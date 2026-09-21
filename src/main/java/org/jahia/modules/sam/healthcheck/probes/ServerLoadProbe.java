package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.modules.sam.load.LoadAverageService;
import org.jahia.modules.sam.load.LoadAverageValue;
import org.jahia.modules.sam.load.provider.JCRNodeCacheLoadAverage;
import org.jahia.modules.sam.load.provider.JCRSessionLoadAverage;
import org.jahia.modules.sam.load.provider.RequestLoadAverage;
import org.jahia.modules.sam.load.provider.ThreadLoadAverage;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component(service = Probe.class, immediate = true)
public class ServerLoadProbe extends AbstractProbe {

    private static final Logger logger = LoggerFactory.getLogger(ServerLoadProbe.class);

    /** The threshold each configuration key falls back to when the operator has not set it. */
    private static final Map<String, Integer> configDefaults = Map.of(
            "requestLoadYellowThreshold", 40,
            "requestLoadRedThreshold", 70,
            "sessionLoadYellowThreshold", 40,
            "sessionLoadRedThreshold", 70,
            "nodeCacheLoadYellowThreshold", 1000,
            "nodeCacheLoadRedThreshold", 2000,
            "threadLoadYellowThreshold", 1000,
            "threadLoadRedThreshold", 1500
    );

    private volatile int requestLoadYellowThreshold = configDefaults.get("requestLoadYellowThreshold");
    private volatile int requestLoadRedThreshold = configDefaults.get("requestLoadRedThreshold");
    private volatile int sessionLoadYellowThreshold = configDefaults.get("sessionLoadYellowThreshold");
    private volatile int sessionLoadRedThreshold = configDefaults.get("sessionLoadRedThreshold");
    private volatile int nodeCacheLoadYellowThreshold = configDefaults.get("nodeCacheLoadYellowThreshold");
    private volatile int nodeCacheLoadRedThreshold = configDefaults.get("nodeCacheLoadRedThreshold");
    private volatile int threadLoadYellowThreshold = configDefaults.get("threadLoadYellowThreshold");
    private volatile int threadLoadRedThreshold = configDefaults.get("threadLoadRedThreshold");

    /**
     * Maps configuration keys to their corresponding setter methods.
     */
    private final Map<String, Consumer<Integer>> configSetters = Map.of(
            "requestLoadYellowThreshold", value -> requestLoadYellowThreshold = value,
            "requestLoadRedThreshold", value -> requestLoadRedThreshold = value,
            "sessionLoadYellowThreshold", value -> sessionLoadYellowThreshold = value,
            "sessionLoadRedThreshold", value -> sessionLoadRedThreshold = value,
            "nodeCacheLoadYellowThreshold", value -> nodeCacheLoadYellowThreshold = value,
            "nodeCacheLoadRedThreshold", value -> nodeCacheLoadRedThreshold = value,
            "threadLoadYellowThreshold", value -> threadLoadYellowThreshold = value,
            "threadLoadRedThreshold", value -> threadLoadRedThreshold = value
    );

    public ServerLoadProbe() {
        super("ServerLoad", "Checks if system load is operating within limits", ProbeSeverity.HIGH);
    }

    @Reference
    private LoadAverageService loadAverageService;

    @Override
    public ProbeStatus getStatus() {

        double oneMinuteRequestLoadAverage =
                loadAverageService.findValue(RequestLoadAverage.class.getName()).orElse(LoadAverageValue.EMPTY).getOneMinuteLoad();
        double oneMinuteCurrentSessionLoad =
                loadAverageService.findValue(JCRSessionLoadAverage.class.getName()).orElse(LoadAverageValue.EMPTY).getOneMinuteLoad();
        double oneMinuteNodeCacheLoad =
                loadAverageService.findValue(JCRNodeCacheLoadAverage.class.getName()).orElse(LoadAverageValue.EMPTY).getOneMinuteLoad();
        double oneMinuteThreadLoad =
                loadAverageService.findValue(ThreadLoadAverage.class.getName()).orElse(LoadAverageValue.EMPTY).getOneMinuteLoad();

        logger.debug("requestYellowThreshold: {}, requestRedThreshold: {}, sessionYellowThreshold: {}, sessionRedThreshold: {}",
                requestLoadYellowThreshold,
                requestLoadRedThreshold,
                sessionLoadYellowThreshold,
                sessionLoadRedThreshold);

        if (oneMinuteRequestLoadAverage < requestLoadYellowThreshold
                && oneMinuteCurrentSessionLoad < sessionLoadYellowThreshold
                && oneMinuteNodeCacheLoad < nodeCacheLoadYellowThreshold
                && oneMinuteThreadLoad < threadLoadYellowThreshold) {
            return new ProbeStatus("Serverload is normal", ProbeStatus.Health.GREEN);
        }
        if (oneMinuteRequestLoadAverage < requestLoadRedThreshold
                && oneMinuteCurrentSessionLoad < sessionLoadRedThreshold
                && oneMinuteNodeCacheLoad < nodeCacheLoadRedThreshold
                && oneMinuteThreadLoad < threadLoadRedThreshold) {
            return new ProbeStatus("Serverload is above normal", ProbeStatus.Health.YELLOW);
        }

        return new ProbeStatus("Serverload is very high", ProbeStatus.Health.RED);
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        // Every value is read before any is assigned, so one unusable value cannot leave half of this
        // configuration applied. A property the operator removed returns its threshold to the default.
        Map<String, Integer> values = new HashMap<>();
        configDefaults.forEach((key, defaultValue) -> values.put(key, parseNumber(config, key, defaultValue)));
        configSetters.forEach((key, setter) -> setter.accept(values.get(key)));
    }
}
