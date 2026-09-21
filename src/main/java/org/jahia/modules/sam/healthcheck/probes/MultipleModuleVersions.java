package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.osgi.BundleState;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component(immediate = true, service = Probe.class)
public class MultipleModuleVersions implements Probe {
    @Override
    public String getName() {
        return "MultipleModuleVersions";
    }

    @Override
    public String getDescription() {
        return "Checks if multiple versions of the same module are present on the Jahia instance";
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.MEDIUM;
    }

    @Override
    public ProbeStatus getStatus() {
        Map<String, Collection<DuplicateInfo>> duplicates = getDuplicates(true);
        if (duplicates.isEmpty()) {
            return new ProbeStatus("No duplicate module found", ProbeStatus.Health.GREEN);
        }

        return new ProbeStatus("The following modules are deployed in multiple versions: " + printDuplicates(duplicates),
                SettingsBean.getInstance().isProductionMode() ? ProbeStatus.Health.YELLOW : ProbeStatus.Health.GREEN);
    }

    protected Map<String, Collection<DuplicateInfo>> getDuplicates(boolean checkModules) {
        Bundle[] bundles = FrameworkService.getBundleContext().getBundles();

        // Collect data
        Map<String, Collection<DuplicateInfo>> collectedBundles = new HashMap<>();
        for (Bundle bundle : bundles) {
            boolean isModule = BundleUtils.isJahiaModuleBundle(bundle);
            if ((isModule && checkModules) || (!isModule && !checkModules)) {
                if (bundle.getState() == Bundle.UNINSTALLED) {
                    // don't check uninstalled bundles
                    continue;
                }

                String symbolicName = bundle.getSymbolicName();
                DuplicateInfo duplicateInfo = new DuplicateInfo(symbolicName, bundle.getVersion().toString(), BundleState.fromInt(bundle.getState()).name());
                if (collectedBundles.containsKey(symbolicName)) {
                    collectedBundles.get(symbolicName).add(duplicateInfo);
                } else {
                    Collection<DuplicateInfo> bundleInfos = new ArrayList<>();
                    bundleInfos.add(duplicateInfo);
                    collectedBundles.put(bundle.getSymbolicName(), bundleInfos);
                }
            }
        }

        // Return duplicates
        return collectedBundles.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    protected String printDuplicates(Map<String, Collection<DuplicateInfo>> duplicates) {
        return duplicates.entrySet().stream().map(entry -> entry.getKey() + ": (" + entry.getValue().stream()
                        .map(duplicateInfo -> duplicateInfo.getVersion() + ": " + duplicateInfo.getState())
                        .collect(Collectors.joining(", ")))
                .collect(Collectors.joining("), ")) + ")";
    }

    protected static class DuplicateInfo {
        private String name;
        private String version;
        private String state;

        public DuplicateInfo(String name, String version, String state) {
            this.name = name;
            this.version = version;
            this.state = state;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }
}
