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
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.healthcheck.probes.RenderingChainProbe;
import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.content.JCRObservationManager;
import org.jahia.settings.SettingsBean;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component(service = EventListener.class, immediate = true)
public class SystemSiteHomeEventListener extends DefaultEventListener {
    private final Logger logger = LoggerFactory.getLogger(SystemSiteHomeEventListener.class);

    private RenderingChainProbe renderingChainProbe;

    public SystemSiteHomeEventListener() {
        setAvailableDuringPublish(true);
    }

    public void setProbe(RenderingChainProbe probe) {
        renderingChainProbe = probe;
    }

    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED +
                Event.PROPERTY_REMOVED + Event.NODE_MOVED;
    }

    @Override
    public String[] getNodeTypes() {
        return new String[] {Constants.JAHIANT_PAGE, Constants.JAHIANT_VIRTUALSITE};
    }

    @Override
    public String getWorkspace() {
        return Constants.LIVE_WORKSPACE;
    }

    @Override
    public Set<Integer> getOperationTypes() {
        return Set.of(JCRObservationManager.IMPORT);
    }

//    @Override public String getPath() {
//        return super.getPath();
//    }

    @Override
    public void onEvent(EventIterator events) {
        while (events.hasNext()) {
            Event event = (Event) events.next();
            try {
                String path = renderingChainProbe.getSiteHomePath();
                if (renderingChainProbe != null && event.getPath().equals(path)) {
                    logger.info("{} has been added - creating rendering test node: event {}", path, event.getType());
                    renderingChainProbe.createRenderingTestNode();
                }
            } catch (RepositoryException e) {
                logger.error("Error processing event", e);
            }
        }
    }

}
