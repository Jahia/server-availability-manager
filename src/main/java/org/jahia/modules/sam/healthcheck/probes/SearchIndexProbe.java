package org.jahia.modules.sam.healthcheck.probes;

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
public class SearchIndexProbe extends AbstractProbe {

    private static final Logger logger = LoggerFactory.getLogger(SearchIndexProbe.class);

    private static final int DEFAULT_YELLOW_THRESHOLD = 10;
    private static final int DEFAULT_RED_THRESHOLD = 50;

    private volatile int queryAVGLastMinuteYellowThreshold = DEFAULT_YELLOW_THRESHOLD;
    private volatile int queryAVGLastMinuteRedThreshold = DEFAULT_RED_THRESHOLD;

    private static final String QUERY_AVG_LAST_MINUTE_YELLOW_THRESHOLD_CONFIG_PROPERTY = "queryAVGLastMinuteYellowThreshold";
    private static final String QUERY_AVG_LAST_MINUTE_RED_THRESHOLD_CONFIG_PROPERTY = "queryAVGLastMinuteRedThreshold";

    private static MessageFormat greenStatus = new MessageFormat("Query AVG ({0}ms) is lower than {1}ms over the last minute. All good here.");
    private static MessageFormat yellowStatus = new MessageFormat("Query AVG ({0}ms) is greater than {1}ms over the last minute.");
    private static MessageFormat redStatus = new MessageFormat("Query AVG ({0}ms) is greater than {1}ms over the last minute. It might be time to reindex.");

    public SearchIndexProbe() {
        super("SearchIndex", "Checks if search indices are too fragmented for performance", ProbeSeverity.HIGH);
    }

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
        // Both values are read before either is assigned, so a second value that is not a number cannot leave
        // half of this configuration applied. A property the operator removed restores the default.
        int yellow = parseNumber(config, QUERY_AVG_LAST_MINUTE_YELLOW_THRESHOLD_CONFIG_PROPERTY, DEFAULT_YELLOW_THRESHOLD);
        int red = parseNumber(config, QUERY_AVG_LAST_MINUTE_RED_THRESHOLD_CONFIG_PROPERTY, DEFAULT_RED_THRESHOLD);
        queryAVGLastMinuteYellowThreshold = yellow;
        queryAVGLastMinuteRedThreshold = red;
    }
}
