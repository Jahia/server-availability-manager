package org.jahia.modules.sam.healthcheck.probes;

import org.apache.commons.lang.StringUtils;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.utils.load.JCRNodeCacheLoadAverage;
import org.jahia.utils.load.JCRSessionLoadAverage;
import org.jahia.utils.load.LoadAverageMonitor;
import org.jahia.utils.load.RequestLoadAverage;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component(service = Probe.class, immediate = true)
public class ServerLoadProbe implements Probe {

    private static final Logger logger = LoggerFactory.getLogger(ServerLoadProbe.class);

    private int requestLoadYellowThreshold = 40;
    private int requestLoadRedThreshold = 70;
    private int sessionLoadYellowThreshold = 40;
    private int sessionLoadRedThreshold = 70;
    private int nodeCacheLoadYellowThreshold = 1000;
    private int nodeCacheLoadRedThreshold = 2000;

    private static final String REQUEST_LOAD_YELLOW_THRESHOLD_CONFIG_PROPERTY = "requestLoadYellowThreshold";
    private static final String REQUEST_LOAD_RED_THRESHOLD_CONFIG_PROPERTY = "requestLoadRedThreshold";
    private static final String SESSION_LOAD_YELLOW_THRESHOLD_CONFIG_PROPERTY = "sessionLoadYellowThreshold";
    private static final String SESSION_LOAD_RED_THRESHOLD_CONFIG_PROPERTY = "sessionLoadRedThreshold";
    private static final String NODECACHE_LOAD_YELLOW_THRESHOLD_CONFIG_PROPERTY = "nodeCacheLoadYellowThreshold";
    private static final String NODECACHE_LOAD_RED_THRESHOLD_CONFIG_PROPERTY = "nodeCacheLoadRedThreshold";

    @Override
    public ProbeStatus getStatus() {

        LoadAverageMonitor monitor = LoadAverageMonitor.getInstance();

        double oneMinuteRequestLoadAverage = monitor.findProviderForName(RequestLoadAverage.NAME).get().getEntry().getOneMinuteLoad();
        double oneMinuteCurrentSessionLoad = monitor.findProviderForName(JCRSessionLoadAverage.NAME).get().getEntry().getOneMinuteLoad();
        double oneMinuteNodeCacheLoad = monitor.findProviderForName(JCRNodeCacheLoadAverage.NAME).get().getEntry().getOneMinuteLoad();
        //double oneMinuteThreadLoad = monitor.findProviderForName(ThreadLoadAverage.NAME).get().getEntry().getOneMinuteLoad();

        logger.debug("requestYellowThreshold: {}, requestRedThreshold: {}, sessionYellowThreshold: {}, sessionRedThreshold: {}",
                requestLoadYellowThreshold,
                requestLoadRedThreshold,
                sessionLoadYellowThreshold,
                sessionLoadRedThreshold);

        if (oneMinuteRequestLoadAverage < requestLoadYellowThreshold && oneMinuteCurrentSessionLoad < sessionLoadYellowThreshold && oneMinuteNodeCacheLoad < nodeCacheLoadYellowThreshold) {
            return new ProbeStatus("Serverload is normal", ProbeStatus.Health.GREEN);
        }
        if (oneMinuteRequestLoadAverage < requestLoadRedThreshold && oneMinuteCurrentSessionLoad < sessionLoadRedThreshold && oneMinuteNodeCacheLoad < nodeCacheLoadRedThreshold) {
            return new ProbeStatus("Serverload is above normal", ProbeStatus.Health.YELLOW);
        }

        return new ProbeStatus("Serverload is very high", ProbeStatus.Health.RED);
    }

    @Override
    public String getDescription() {
        return "Checks if system load is operating within limits";
    }

    @Override
    public String getName() {
        return "ServerLoad";
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.HIGH;
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        if (config.containsKey(REQUEST_LOAD_YELLOW_THRESHOLD_CONFIG_PROPERTY) && !StringUtils.isEmpty(String.valueOf(config.containsKey(REQUEST_LOAD_YELLOW_THRESHOLD_CONFIG_PROPERTY)))) {
            requestLoadYellowThreshold = Integer.parseInt(String.valueOf(config.get(REQUEST_LOAD_YELLOW_THRESHOLD_CONFIG_PROPERTY)));
        }
        if (config.containsKey(REQUEST_LOAD_RED_THRESHOLD_CONFIG_PROPERTY) && !StringUtils.isEmpty(String.valueOf(config.containsKey(REQUEST_LOAD_RED_THRESHOLD_CONFIG_PROPERTY)))) {
            requestLoadRedThreshold = Integer.parseInt(String.valueOf(config.get(REQUEST_LOAD_RED_THRESHOLD_CONFIG_PROPERTY)));
        }
        if (config.containsKey(SESSION_LOAD_YELLOW_THRESHOLD_CONFIG_PROPERTY) && !StringUtils.isEmpty(String.valueOf(config.containsKey(SESSION_LOAD_YELLOW_THRESHOLD_CONFIG_PROPERTY)))) {
            sessionLoadYellowThreshold = Integer.parseInt(String.valueOf(config.get(SESSION_LOAD_YELLOW_THRESHOLD_CONFIG_PROPERTY)));
        }
        if (config.containsKey(SESSION_LOAD_RED_THRESHOLD_CONFIG_PROPERTY) && !StringUtils.isEmpty(String.valueOf(config.containsKey(SESSION_LOAD_RED_THRESHOLD_CONFIG_PROPERTY)))) {
            sessionLoadRedThreshold = Integer.parseInt(String.valueOf(config.get(SESSION_LOAD_RED_THRESHOLD_CONFIG_PROPERTY)));
        }
        if (config.containsKey(NODECACHE_LOAD_YELLOW_THRESHOLD_CONFIG_PROPERTY) && !StringUtils.isEmpty(String.valueOf(config.containsKey(NODECACHE_LOAD_YELLOW_THRESHOLD_CONFIG_PROPERTY)))) {
            nodeCacheLoadYellowThreshold = Integer.parseInt(String.valueOf(config.get(NODECACHE_LOAD_YELLOW_THRESHOLD_CONFIG_PROPERTY)));
        }
        if (config.containsKey(NODECACHE_LOAD_RED_THRESHOLD_CONFIG_PROPERTY) && !StringUtils.isEmpty(String.valueOf(config.containsKey(NODECACHE_LOAD_RED_THRESHOLD_CONFIG_PROPERTY)))) {
            nodeCacheLoadRedThreshold = Integer.parseInt(String.valueOf(config.get(NODECACHE_LOAD_RED_THRESHOLD_CONFIG_PROPERTY)));
        }
    }
}
