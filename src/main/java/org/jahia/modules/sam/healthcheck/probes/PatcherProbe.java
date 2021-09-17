package org.jahia.modules.sam.healthcheck.probes;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.tools.patches.Patcher;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

@Component(service = Probe.class, immediate = true)
public class PatcherProbe implements Probe {

    private static final Logger logger = LoggerFactory.getLogger(PatcherProbe.class);


    @Override
    public ProbeStatus getStatus() {
        File lookupFolder = Patcher.getInstance().getPatchesFolder();
        if (lookupFolder == null) {
            return ProbeStatus.GREEN;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Looking up failed patches in the folder {}", lookupFolder);
        }
        List<File> patches = new LinkedList<>(FileUtils.listFiles(
                lookupFolder,
                new SuffixFileFilter(Patcher.SUFFIX_FAILED),
                TrueFileFilter.INSTANCE
        ));

        if (!patches.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug(patches.size() > 1 ? "{} Failed patches were found:" : "{} Failed patch was found:", patches.size());
                patches.forEach(file -> logger.debug("Patch {} has failed.", file.getName()));
            }
            return ProbeStatus.RED;
        }
        return ProbeStatus.GREEN;
    }

    @Override
    public String getDescription() {
        return "Check if any patch failed";
    }

    @Override
    public String getName() {
        return "PatchFailures";
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.CRITICAL;
    }
}
