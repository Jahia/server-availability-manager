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

    private static final int DEFAULT_REQUEST_LOAD_YELLOW = 40;
    private static final int DEFAULT_REQUEST_LOAD_RED = 70;
    private static final int DEFAULT_SESSION_LOAD_YELLOW = 40;
    private static final int DEFAULT_SESSION_LOAD_RED = 70;
    private static final int DEFAULT_NODE_CACHE_LOAD_YELLOW = 1000;
    private static final int DEFAULT_NODE_CACHE_LOAD_RED = 2000;
    private static final int DEFAULT_THREAD_LOAD_YELLOW = 1000;
    private static final int DEFAULT_THREAD_LOAD_RED = 1500;

    private volatile int requestLoadYellowThreshold = DEFAULT_REQUEST_LOAD_YELLOW;
    private volatile int requestLoadRedThreshold = DEFAULT_REQUEST_LOAD_RED;
    private volatile int sessionLoadYellowThreshold = DEFAULT_SESSION_LOAD_YELLOW;
    private volatile int sessionLoadRedThreshold = DEFAULT_SESSION_LOAD_RED;
    private volatile int nodeCacheLoadYellowThreshold = DEFAULT_NODE_CACHE_LOAD_YELLOW;
    private volatile int nodeCacheLoadRedThreshold = DEFAULT_NODE_CACHE_LOAD_RED;
    private volatile int threadLoadYellowThreshold = DEFAULT_THREAD_LOAD_YELLOW;
    private volatile int threadLoadRedThreshold = DEFAULT_THREAD_LOAD_RED;

    /**
     * What each configuration key defaults to, and what it sets. One entry per key, so a key cannot appear in
     * a list of defaults without appearing in a list of setters.
     */
    private final Map<String, Threshold> thresholds = Map.of(
            "requestLoadYellowThreshold", new Threshold(DEFAULT_REQUEST_LOAD_YELLOW, v -> requestLoadYellowThreshold = v),
            "requestLoadRedThreshold", new Threshold(DEFAULT_REQUEST_LOAD_RED, v -> requestLoadRedThreshold = v),
            "sessionLoadYellowThreshold", new Threshold(DEFAULT_SESSION_LOAD_YELLOW, v -> sessionLoadYellowThreshold = v),
            "sessionLoadRedThreshold", new Threshold(DEFAULT_SESSION_LOAD_RED, v -> sessionLoadRedThreshold = v),
            "nodeCacheLoadYellowThreshold", new Threshold(DEFAULT_NODE_CACHE_LOAD_YELLOW, v -> nodeCacheLoadYellowThreshold = v),
            "nodeCacheLoadRedThreshold", new Threshold(DEFAULT_NODE_CACHE_LOAD_RED, v -> nodeCacheLoadRedThreshold = v),
            "threadLoadYellowThreshold", new Threshold(DEFAULT_THREAD_LOAD_YELLOW, v -> threadLoadYellowThreshold = v),
            "threadLoadRedThreshold", new Threshold(DEFAULT_THREAD_LOAD_RED, v -> threadLoadRedThreshold = v)
    );

    /** One configurable threshold: what it falls back to, and where its value goes. */
    private static final class Threshold {
        private final int defaultValue;
        private final Consumer<Integer> setter;

        Threshold(int defaultValue, Consumer<Integer> setter) {
            this.defaultValue = defaultValue;
            this.setter = setter;
        }
    }

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
        thresholds.forEach((key, threshold) -> values.put(key, parseNumber(config, key, threshold.defaultValue)));
        thresholds.forEach((key, threshold) -> threshold.setter.accept(values.get(key)));
    }
}
