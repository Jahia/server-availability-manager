package org.jahia.modules.sam.healthcheck.probes;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.api.stats.RepositoryStatistics;
import org.apache.jackrabbit.core.JahiaRepositoryImpl;
import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.store.FSDirectory;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.impl.jackrabbit.SpringJackrabbitRepository;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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


    private int queryAVGLastMinuteYellowThreshold = 5;
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
        if (queryAVG > queryAVGLastMinuteYellowThreshold) {
            return new ProbeStatus(yellowStatus.format(new Object[]{queryAVG, queryAVGLastMinuteYellowThreshold}), ProbeStatus.Health.YELLOW);
        } else if (queryAVG > queryAVGLastMinuteRedThreshold) {
            return new ProbeStatus(redStatus.format(new Object[]{queryAVG, queryAVGLastMinuteRedThreshold}), ProbeStatus.Health.RED);
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

//            File repositoryHome = SettingsBean.getInstance().getRepositoryHome();
//            List<IndexStatistics> indexStatistics = Arrays.asList(new File(repositoryHome, "index"), new File(repositoryHome, "workspaces/default/index"), new File(repositoryHome, "workspaces/live/index")).parallelStream().map(file -> new IndexStatistics(file)).collect(Collectors.toList());
//
//            return new ProbeStatus(MessageFormat.format("All indices are OK. Query AVG : {0,number} ms. {1}", queryAVG, indexStatistics.stream().map(Objects::toString).collect(Collectors.joining("."))), ProbeStatus.Health.GREEN);

    private class IndexStatistics {
        private final File file;
        private final Optional<Integer> numberOfSegments;
        private final boolean clean;
        private BigInteger sizeOfDirectory;

        public IndexStatistics(File file) {
            this.file = file;
            this.sizeOfDirectory = FileUtils.sizeOfDirectoryAsBigInteger(file);
            List<CheckIndex.Status> statusList = Arrays.stream(file.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY)).map(file1 -> {
                try {
                    FSDirectory fsDirectory = FSDirectory.open(file1);
                    CheckIndex checkIndex = new CheckIndex(fsDirectory);
                    return checkIndex.checkIndex();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
            this.numberOfSegments = statusList.stream().map(status -> status.numSegments).reduce(Integer::sum);
            this.clean = statusList.stream().allMatch(status -> status.clean);
        }

        @Override
        public String toString() {
            try {
                return "[" +
                        "file=" + file.getCanonicalPath() + " is " + (clean ? "clean" : "not clean") +
                        ", sizeOfDirectory=" + FileUtils.byteCountToDisplaySize(sizeOfDirectory) +
                        ", numberOfSegments=" + numberOfSegments.orElse(0) + "]";
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
