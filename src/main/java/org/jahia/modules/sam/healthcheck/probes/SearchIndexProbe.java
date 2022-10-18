package org.jahia.modules.sam.healthcheck.probes;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.jackrabbit.api.stats.RepositoryStatistics;
import org.apache.jackrabbit.core.JahiaRepositoryImpl;
import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.store.FSDirectory;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.impl.jackrabbit.SpringJackrabbitRepository;
import org.jahia.settings.SettingsBean;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return ProbeSeverity.CRITICAL;
    }


    @Override
    public ProbeStatus getStatus() {
        JahiaRepositoryImpl repository = (JahiaRepositoryImpl) ((SpringJackrabbitRepository) JCRSessionFactory.getInstance().getDefaultProvider().getRepository()).getRepository();
        RepositoryStatistics repositoryStatistics = repository.getContext().getRepositoryStatistics();
        double queryAVG = Arrays.stream(repositoryStatistics.getTimeSeries(RepositoryStatistics.Type.QUERY_AVERAGE).getValuePerHour()).average().orElse(Double.NaN);
        if (queryAVG > 100.0) {
            return new ProbeStatus("Query AVG is greater than 100ms over the last hour. It might be time to reindex.", ProbeStatus.Health.YELLOW);
        }
        try {
            File repositoryHome = SettingsBean.getInstance().getRepositoryHome();
            List<IndexStatistics> indexStatistics = Arrays.asList(new File(repositoryHome, "index"), new File(repositoryHome, "workspaces/default/index"), new File(repositoryHome, "workspaces/live/index")).parallelStream().map(file -> new IndexStatistics(file)).collect(Collectors.toList());

            return new ProbeStatus(MessageFormat.format("All indices are OK. Query AVG : {0,number} ms. {1}", queryAVG, indexStatistics.stream().map(Objects::toString).collect(Collectors.joining("."))), ProbeStatus.Health.GREEN);
        } catch (IOException e) {
            return new ProbeStatus("Error while checking indices: " + e.getMessage(), ProbeStatus.Health.YELLOW);
        }
    }

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
