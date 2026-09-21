package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.settings.SettingsBean;
import org.osgi.service.component.annotations.Component;

import java.util.Collection;
import java.util.Map;

@Component(immediate = true, service = Probe.class)
public class MultipleBundleVersions extends MultipleModuleVersions {

    public MultipleBundleVersions() {
        super("MultipleBundleVersions", "Checks if multiple versions of the same bundle are present on the Jahia instance",
                ProbeSeverity.HIGH);
    }

    @Override
    public ProbeStatus getStatus() {
        Map<String, Collection<DuplicateInfo>> duplicates = getDuplicates(false);
        if (duplicates.isEmpty()) {
            return new ProbeStatus("No duplicate bundle found", ProbeStatus.Health.GREEN);
        }

        return new ProbeStatus(" The following bundles are deployed in multiple versions: " + printDuplicates(duplicates),
                SettingsBean.getInstance().isProductionMode() ? ProbeStatus.Health.RED : ProbeStatus.Health.YELLOW);
    }
}
