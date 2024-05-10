package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleState;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.modules.sam.ProbeActivator;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.settings.SettingsBean;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.*;

/**
 * Snapshots probe works only in PRODUCTION mode and allows to see if there are any modules that are versioned as snapshots on the system.
 */
@Component(service = ProbeActivator.class, immediate = true)
public class SnapshotInProductionProbe implements Probe, ProbeActivator {

    public static final String PROBE_NAME = "Snapshot";
    private JahiaTemplateManagerService templateManagerService;
    private ServiceRegistration<Probe> serviceRegistration = null;

    @Activate
    public void activate(BundleContext ctx) {
        // Activates probe only in production mode, we don't see it in other modes
        if (SettingsBean.getInstance().isProductionMode()) {
            activateSnapshotProbe(ctx);
        }
    }

    @Deactivate
    public void deactivate() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

    @Reference
    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    public void activateSnapshotProbe(BundleContext ctx) {
        serviceRegistration = (ServiceRegistration<Probe>) ctx.registerService(Probe.class.getName(), this, null);
    }

    @Override
    public String getName() {
        return PROBE_NAME;
    }

    @Override
    public String getDescription() {
        return "Checks if any of the modules on a Jahia instance are versioned as snapshot";
    }

    @Override
    public ProbeStatus getStatus() {
        String report = getJsonReport();

        if (report != null) {
            return new ProbeStatus(report, ProbeStatus.Health.YELLOW);
        }

        return new ProbeStatus("There are no snapshots", ProbeStatus.Health.GREEN);
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.MEDIUM;
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        // Do nothing
    }

    private String getJsonReport() {
        String jsonReport = null;
        JSONArray snapshots = new JSONArray();
        Map<Bundle, ModuleState> moduleStatesByBundle = templateManagerService.getModuleStates();

        for (Bundle bundle : moduleStatesByBundle.keySet()) {
            JahiaTemplatesPackage pkg = BundleUtils.getModule(bundle);
            ModuleVersion version = pkg.getVersion();
            if (version.isSnapshot()) {
                JSONObject o = new JSONObject();
                o.put("module", pkg.getName());
                o.put("version", pkg.getVersion().toString());
                o.put("state", pkg.getState().getState().toString());
                snapshots.put(o);
            }
        }

        if (!snapshots.isEmpty()) {
            JSONObject report = new JSONObject();
            report.put("snapshotCount", snapshots.length());
            report.put("snapshots", snapshots);
            jsonReport = report.toString();
        }

        return jsonReport;
    }
}
