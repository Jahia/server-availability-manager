package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.modulemanager.spi.BundleService;
import org.jahia.settings.SettingsBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(service = Probe.class, immediate = true)
public class ClusterConsistencyProbe implements Probe {

    private static final Logger logger = LoggerFactory.getLogger(ClusterConsistencyProbe.class);

    private String clusterGroup = "default";

    @Activate
    public void activate() {
        logger.info("Activated: " + getName());
    }



    @Override
    public ProbeStatus getStatus() {
        if (!SettingsBean.getInstance().isClusterActivated()) {
            return new ProbeStatus("Cluster is not activated", ProbeStatus.Health.GREEN);
        }

        BundleService clusteredBundleService = BundleUtils.getOsgiService("org.jahia.services.modulemanager.spi.BundleService");

        if (clusteredBundleService != null) {
            Map<String, Map<String, BundleService.BundleInformation>> result = clusteredBundleService.getAllInfos(clusterGroup);

            String baseClusterNodeId = result.keySet().iterator().next();
            Map<String, BundleService.BundleInformation> baseResult  = result.get(baseClusterNodeId);
            Map<String, JSONArray> findings = new HashMap<>();

            // Select node with most bundles to be the starting point
            for (Map.Entry<String, Map<String, BundleService.BundleInformation>> entry : result.entrySet()) {
                String clusterNodeId = entry.getKey();
                findings.put(clusterNodeId, null);
                if (baseResult.size() < entry.getValue().size()) {
                    baseResult = entry.getValue();
                    baseClusterNodeId = clusterNodeId;
                }
            }


            // Crosscheck status of every module in cluster nodes, look only at ModuleInformation
            for (Map.Entry<String, BundleService.BundleInformation> entry : baseResult.entrySet()) {
                String moduleId = entry.getKey();
                BundleService.BundleInformation bi = entry.getValue();

                if (bi instanceof BundleService.ModuleInformation) {
                    List<ModuleState> ms = new ArrayList<>();
                    ms.add(new ModuleState((BundleService.ModuleInformation) bi, baseClusterNodeId, moduleId));

                    for (Map.Entry<String, Map<String, BundleService.BundleInformation>> entry2 : result.entrySet()) {
                        String clusterNodeId = entry2.getKey();
                        if (!clusterNodeId.equals(baseClusterNodeId)) {
                            Map<String, BundleService.BundleInformation> currentResult = entry2.getValue();
                            ms.add(new ModuleState((BundleService.ModuleInformation) currentResult.get(moduleId), clusterNodeId, moduleId));
                        }
                    }

                    if (!areStatesConsistent(ms)) {
                        recordMessage(ms, findings);
                    }
                }
            }

            return getStatus(findings);
        }

        return new ProbeStatus("Failed to find ClusteredBundleService", ProbeStatus.Health.RED);
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.HIGH;
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        if (config.containsKey("clusterGroup")) {
            clusterGroup = ((String) config.get("clusterGroup"));
        }
    }

    @Override
    public String getName() {
        return "ClusterConsistencyProbe";
    }

    @Override
    public String getDescription() {
        return "Finds module state inconsistency within cluster";
    }

    private boolean areStatesConsistent(List<ModuleState> moduleStateList) {
        boolean consistent = true;

        for (ModuleState ms1 : moduleStateList) {
            for (ModuleState ms2 : moduleStateList) {
                BundleService.ModuleInformation mi = ms1.getModuleInformation();
                BundleService.ModuleInformation mi2 = ms2.getModuleInformation();
                consistent = mi.getModuleState().equals(mi2.getModuleState()) && mi.getOsgiState().equals(mi2.getOsgiState());

                if (!consistent) {
                    break;
                }
            }
        }

        return consistent;
    }

    private void recordMessage(List<ModuleState> moduleStateList, Map<String, JSONArray> findings) {
        for (ModuleState ms : moduleStateList) {
            JSONArray arr = findings.get(ms.getClusterNodeId());

            if (arr == null) {
                findings.put(ms.getClusterNodeId(), new JSONArray());
                arr = findings.get(ms.getClusterNodeId());
            }

            JSONObject obj = new JSONObject();
            try {
                obj.put("clusterNodeId", ms.getClusterNodeId());
                obj.put("module", ms.getModulePathId());
                obj.put("osgiState", ms.getModuleInformation().getOsgiState().name());
                obj.put("moduleState", ms.getModuleInformation().getModuleState().name());
            } catch (JSONException e) {
                logger.error("Failed to create json object for cluster status report", e);
            }

            arr.put(obj);
        }
    }

    private ProbeStatus getStatus(Map<String, JSONArray> findings) {
        JSONObject obj = new JSONObject();
        ProbeStatus.Health health = ProbeStatus.Health.GREEN;

        for (Map.Entry<String, JSONArray> entry : findings.entrySet()) {
            JSONArray arr = entry.getValue();

            try {
                if (arr == null) {
                    obj.put(entry.getKey(), "Not issues found on this node");
                } else {
                    health = ProbeStatus.Health.YELLOW;
                    obj.put(entry.getKey(), arr);
                }
            } catch (JSONException e) {
                logger.error("Failed to build json response for cluster status", e);
            }
        }

        return new ProbeStatus(obj.toString(), health);
    }

    private class ModuleState {
        private BundleService.ModuleInformation moduleInformation;
        private String clusterNodeId;
        private String modulePathId;

        public ModuleState(BundleService.ModuleInformation moduleInformation, String clusterNodeId, String modulePathId) {
            this.moduleInformation = moduleInformation;
            this.clusterNodeId = clusterNodeId;
            this.modulePathId = modulePathId;
        }

        public BundleService.ModuleInformation getModuleInformation() {
            return moduleInformation;
        }

        public String getClusterNodeId() {
            return clusterNodeId;
        }

        public String getModulePathId() {
            return modulePathId;
        }
    }
}
