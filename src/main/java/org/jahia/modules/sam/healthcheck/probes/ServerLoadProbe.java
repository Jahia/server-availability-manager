package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.utils.JCRSessionLoadAverage;
import org.jahia.utils.RequestLoadAverage;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Component(service = Probe.class, immediate = true)
public class ServerLoadProbe implements Probe {

    private static final Logger logger = LoggerFactory.getLogger(ServerLoadProbe.class);

    // Threshold default values
    private int requestLoadYellowThreshold = 40;
    private int requestLoadRedThreshold = 70;
    private int sessionLoadYellowThreshold = 40;
    private int sessionLoadRedThreshold = 70;

    @Override
    public ProbeStatus getStatus() {

        JSONObject loadAverageJson = new JSONObject();
        try {
            loadAverageJson.put("oneMinuteRequestLoadAverage", RequestLoadAverage.getInstance().getOneMinuteLoad());
            loadAverageJson.put("oneMinuteCurrentSessionLoad", JCRSessionLoadAverage.getInstance().getOneMinuteLoad());
        } catch (JSONException ex) {
            logger.error("Impossible to generate the JSON", ex);
        }
        try {
            logger.debug("requestYellowThreshold: {}, requestRedThreshold: {}, sessionYellowThreshold: {}, sessionRedThreshold: {}",
                    requestLoadYellowThreshold,
                    requestLoadRedThreshold,
                    sessionLoadYellowThreshold,
                    sessionLoadRedThreshold);
            if (!loadAverageJson.has("oneMinuteRequestLoadAverage") || !loadAverageJson.has("oneMinuteCurrentSessionLoad")) {
                logger.warn("Impossible to read request load values {} {}", loadAverageJson.has("oneMinuteRequestLoadAverage"), loadAverageJson.has("oneMinuteCurrentSessionLoad"));
                return ProbeStatus.YELLOW;
            }
            if (loadAverageJson.getInt("oneMinuteRequestLoadAverage") < requestLoadYellowThreshold && loadAverageJson.getInt("oneMinuteCurrentSessionLoad") < sessionLoadYellowThreshold) {
                return ProbeStatus.GREEN;
            }
            if (loadAverageJson.getInt("oneMinuteRequestLoadAverage") < requestLoadRedThreshold && loadAverageJson.getInt("oneMinuteCurrentSessionLoad") < sessionLoadRedThreshold) {
                return ProbeStatus.YELLOW;
            }

        } catch (JSONException ex) {
            logger.error("Impossible to read the JSON", ex);
        }
        return ProbeStatus.RED;
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
        requestLoadYellowThreshold = (config.containsKey("requestLoadYellowThreshold") ? Integer.parseInt("requestLoadYellowThreshold") : requestLoadYellowThreshold);
        requestLoadRedThreshold = (config.containsKey("requestLoadRedThreshold") ? Integer.parseInt("requestLoadRedThreshold") : requestLoadRedThreshold);
        sessionLoadYellowThreshold = (config.containsKey("sessionLoadYellowThreshold") ? Integer.parseInt("sessionLoadYellowThreshold") : sessionLoadYellowThreshold);
        sessionLoadRedThreshold = (config.containsKey("sessionLoadRedThreshold") ? Integer.parseInt("sessionLoadRedThreshold") : sessionLoadRedThreshold);
    }
}
