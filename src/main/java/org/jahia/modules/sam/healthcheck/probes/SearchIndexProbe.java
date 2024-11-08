package org.jahia.modules.sam.healthcheck.probes;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.api.stats.RepositoryStatistics;
import org.apache.jackrabbit.core.JahiaRepositoryImpl;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.impl.jackrabbit.SpringJackrabbitRepository;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;

@Component(service = Probe.class, immediate = true)
public class SearchIndexProbe implements Probe {
    private static final Logger logger = LoggerFactory.getLogger(SearchIndexProbe.class);

    @Override
    public String getName() {
        return "SearchIndex";
    }

    @Override
    public String getDescription() {
        return "Checks if search indices are too fragmented for performance";
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.HIGH;
    }


    private int queryAVGLastMinuteYellowThreshold = 10;
    private int queryAVGLastMinuteRedThreshold = 50;

    private static final String QUERY_AVG_LAST_MINUTE_YELLOW_THRESHOLD_CONFIG_PROPERTY = "queryAVGLastMinuteYellowThreshold";
    private static final String QUERY_AVG_LAST_MINUTE_RED_THRESHOLD_CONFIG_PROPERTY = "queryAVGLastMinuteRedThreshold";

    private static MessageFormat greenStatus = new MessageFormat("Query AVG ({0}ms) is lower than {1}ms over the last minute. All good here.");
    private static MessageFormat yellowStatus = new MessageFormat("Query AVG ({0}ms) is greater than {1}ms over the last minute.");
    private static MessageFormat redStatus = new MessageFormat("Query AVG ({0}ms) is greater than {1}ms over the last minute. It might be time to reindex.");

    @Override
    public ProbeStatus getStatus() {
        JahiaRepositoryImpl repository = (JahiaRepositoryImpl) ((SpringJackrabbitRepository) JCRSessionFactory.getInstance().getDefaultProvider().getRepository()).getRepository();
        RepositoryStatistics repositoryStatistics = repository.getContext().getRepositoryStatistics();
        double queryAVG = Arrays.stream(repositoryStatistics.getTimeSeries(RepositoryStatistics.Type.QUERY_AVERAGE).getValuePerMinute()).average().orElse(Double.NaN);
        if (Double.isNaN(queryAVG) || queryAVG > queryAVGLastMinuteRedThreshold) {
            return new ProbeStatus(redStatus.format(new Object[]{queryAVG, queryAVGLastMinuteRedThreshold}), ProbeStatus.Health.RED);
        } else if (queryAVG > queryAVGLastMinuteYellowThreshold) {
            return new ProbeStatus(yellowStatus.format(new Object[]{queryAVG, queryAVGLastMinuteYellowThreshold}), ProbeStatus.Health.YELLOW);
        }
        return new ProbeStatus(greenStatus.format(new Object[]{queryAVG, queryAVGLastMinuteYellowThreshold}), ProbeStatus.Health.GREEN);
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        if (config.containsKey(QUERY_AVG_LAST_MINUTE_YELLOW_THRESHOLD_CONFIG_PROPERTY) && !StringUtils.isEmpty(String.valueOf(config.containsKey(QUERY_AVG_LAST_MINUTE_YELLOW_THRESHOLD_CONFIG_PROPERTY)))) {
            queryAVGLastMinuteYellowThreshold = Integer.parseInt(String.valueOf(config.get(QUERY_AVG_LAST_MINUTE_YELLOW_THRESHOLD_CONFIG_PROPERTY)));
        }
        if (config.containsKey(QUERY_AVG_LAST_MINUTE_RED_THRESHOLD_CONFIG_PROPERTY) && !StringUtils.isEmpty(String.valueOf(config.containsKey(QUERY_AVG_LAST_MINUTE_RED_THRESHOLD_CONFIG_PROPERTY)))) {
            queryAVGLastMinuteRedThreshold = Integer.parseInt(String.valueOf(config.get(QUERY_AVG_LAST_MINUTE_RED_THRESHOLD_CONFIG_PROPERTY)));
        }
    }
}
