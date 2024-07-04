/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.modules.sam.events;

import org.jahia.api.Constants;
import org.jahia.modules.sam.healthcheck.probes.RenderingChainProbe;
import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.content.JCRObservationManager;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.Set;

public class SystemSiteHomeEventListener extends DefaultEventListener {
    private final Logger logger = LoggerFactory.getLogger(SystemSiteHomeEventListener.class);

    private RenderingChainProbe renderingChainProbe;
    private String eventNodePath = "/sites/systemsite/home"; // default

    public void setProbe(RenderingChainProbe probe) {
        renderingChainProbe = probe;
        eventNodePath = probe.getSiteHomePath();
    }

    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED;
    }

    @Override
    public String getWorkspace() {
        return Constants.LIVE_WORKSPACE;
    }

    @Override
    public Set<Integer> getOperationTypes() {
        return Set.of(JCRObservationManager.IMPORT);
    }

    @Override public boolean isAvailableDuringPublish() {
        return true;
    }

    @Override
    public String getPath() {
        return eventNodePath;
    }

    @Override public boolean isDeep() {
        return false;
    }

    @Override
    public void onEvent(EventIterator events) {
        if (SettingsBean.getInstance().isClusterActivated() && !SettingsBean.getInstance().isProcessingServer()) {
            return;
        }

        if (events.hasNext()) {
            Event ev = events.nextEvent();
            if (renderingChainProbe != null) {
                logger.info("{} has been added - creating rendering test node", eventNodePath);
                renderingChainProbe.createRenderingTestNode();
            } else {
                logger.error("Unable to create rendering test node from event - probe not initialized.");
            }
        }
    }

}
