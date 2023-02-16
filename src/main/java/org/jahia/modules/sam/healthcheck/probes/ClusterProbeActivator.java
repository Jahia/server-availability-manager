package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.modules.sam.ProbeStatus;
import org.jahia.settings.SettingsBean;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(immediate = true)
public class ClusterProbeActivator {


    @Activate
    public void activate(ComponentContext componentContext) {
        if (SettingsBean.getInstance().isClusterActivated()) {
            componentContext.enableComponent("org.jahia.modules.sam.healthcheck.probes.ClusterConsistencyProbe");
        }
    }

    @Deactivate
    public void deactivate(ComponentContext componentContext) {
        if (SettingsBean.getInstance().isClusterActivated()) {
            componentContext.disableComponent("org.jahia.modules.sam.healthcheck.probes.ClusterConsistencyProbe");
        }
    }

}
