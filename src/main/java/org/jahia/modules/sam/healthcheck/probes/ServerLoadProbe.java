package org.jahia.modules.sam.healthcheck.probes;

import org.apache.commons.lang.StringUtils;
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

import java.util.Map;
import java.util.function.Consumer;

@Component(service = Probe.class, immediate = true)
public class ServerLoadProbe implements Probe {

    private static final Logger logger = LoggerFactory.getLogger(ServerLoadProbe.class);

    private int requestLoadYellowThreshold = 40;
    private int requestLoadRedThreshold = 70;
    private int sessionLoadYellowThreshold = 40;
    private int sessionLoadRedThreshold = 70;
    private int nodeCacheLoadYellowThreshold = 1000;
    private int nodeCacheLoadRedThreshold = 2000;
    private int threadLoadYellowThreshold = 1000;
    private int threadLoadRedThreshold = 1500;

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
            "theadLoadRedThreshold", value -> threadLoadRedThreshold = value
    );

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
        configSetters.forEach((key, setter) -> {
            if (config.containsKey(key) && !StringUtils.isEmpty(String.valueOf(config.get(key)))) {
                setter.accept(Integer.parseInt(String.valueOf(config.get(key))));
            }
        });

    }
}
