package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.api.Constants;
import org.jahia.bin.Render;
import org.jahia.modules.graphql.provider.dxm.node.NodeHelper;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.modules.sam.events.SystemSiteHomeEventListener;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.events.JournalEventReader;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.settings.SettingsBean;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;

@Component(service = Probe.class, immediate = true)
public class RenderingChainProbe implements Probe {

    private final Logger logger = LoggerFactory.getLogger(RenderingChainProbe.class);
    private JournalEventReader journalEventReader;
    private JahiaTemplateManagerService templateManagerService;
    private final SystemSiteHomeEventListener eventListener = new SystemSiteHomeEventListener();

    private final String testText = "Rendering Chain Test initialized";
    private final String siteHomePath = "/sites/" + JahiaSitesService.SYSTEM_SITE_KEY + "/home";
    private String nodeName;
    private String nodePath;


    /**
     * Create the rendering chain test node in LIVE workspace
     * @throws RepositoryException
     */
    @Activate
    public void start() throws RepositoryException {
        templateManagerService = BundleUtils.getOsgiService(JahiaTemplateManagerService.class, null);
        initNodePathName();

        // register for system site listener if rendering chain test node not yet created
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.LIVE_WORKSPACE, null, session -> {
            if (!session.nodeExists(getSiteHomePath())) {
                logger.debug("Rendering Chain test node not found, registering for SystemSiteEventListener");
                eventListener.setProbe(this);
                templateManagerService.getTemplatePackageRegistry().handleJCREventListener(eventListener, true);
            } else {
                createRenderingTestNode();
            }
            return null;
        });
    }

    private void initNodePathName() {
        boolean isClusterActivated = SettingsBean.getInstance().isClusterActivated();
        String nodeSuffix = (isClusterActivated && journalEventReader != null) ? journalEventReader.getNodeId() : "";
        nodeName = "renderingChainTest" + nodeSuffix;
        nodePath = siteHomePath + "/" + nodeName;
    }

    public void createRenderingTestNode() {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.LIVE_WORKSPACE, null, session -> {
                if (!session.nodeExists(siteHomePath)) {
                    logger.debug("{} does not exist yet; unable to create rendering chain test node for now", siteHomePath);
                    return null;
                }

                JCRNodeWrapper testNode;
                if (!session.nodeExists(nodePath)) {
                    logger.debug("Creating rendering chain test node at {}", nodePath);
                    JCRNodeWrapper home = session.getNode(siteHomePath);
                    testNode = home.addNode(nodeName, "sam:renderingChain");
                    testNode.setProperty("jcr:title", "Rendering Chain Test Node");
                    testNode.setProperty("text", testText);
                    session.save();

                    // unregister event listener after creation
                    if (templateManagerService != null) {
                        templateManagerService.getTemplatePackageRegistry().handleJCREventListener(eventListener, false);
                    }

                    logger.info("Rendering chain test node created at {}", nodePath);
                }
                return null;
            });
        } catch (RepositoryException e) {
            logger.error("Error creating rendering chain test node", e);
        }
    }

    public String getSiteHomePath() {
        return siteHomePath;
    }

    @Deactivate
    public void stop() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.LIVE_WORKSPACE, null, session -> {
            if (session.nodeExists(nodePath)) {
                logger.debug("Removing rendering chain test node at {}", nodePath);
                session.getNode(nodePath).remove();
                session.save();
            }
            return null;
        });
    }

    @Reference
    public void setJournalEventReader(JournalEventReader journalEventReader) {
        this.journalEventReader = journalEventReader;
    }

    @Override
    public String getName() {
        return "RenderingChain";
    }

    @Override
    public String getDescription() {
        return "Validate that the rendering chain is working properly.";
    }

    @Override
    public ProbeStatus getStatus(HttpServletRequest request, HttpServletResponse response) {
        RenderService renderService = (RenderService) SpringContextSingleton.getBean("RenderService");
        JahiaSitesService jahiaSitesService = (JahiaSitesService) SpringContextSingleton.getBean("JahiaSitesService");
        if (renderService == null) {
            return new ProbeStatus("RenderService not found", ProbeStatus.Health.RED);
        }
        if (jahiaSitesService == null) {
            return new ProbeStatus("JahiaSitesService not found", ProbeStatus.Health.RED);
        }

        String renderingResult = null;
        try {
            JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
            RenderContext renderContext = new RenderContext(request, response, sessionFactory.getCurrentUser());
            JCRSessionWrapper currentUserSession = sessionFactory.getCurrentUserSession(Constants.LIVE_WORKSPACE, null);

            if (!currentUserSession.nodeExists(nodePath)) {
                // something wrong happened in the setup/activate, not necessarily with the rendering chain
                return new ProbeStatus("Rendering Chain test node " + nodePath + " not found", ProbeStatus.Health.YELLOW);
            }

            JCRNodeWrapper testNode = currentUserSession.getNode(nodePath);
            JCRNodeWrapper mainNode = NodeHelper.getNodeInLanguage(testNode, "en");
            Resource r = new Resource(mainNode, "html", "default", "module");
            renderContext.setMainResource(r);
            renderContext.setServletPath(Render.getRenderServletPath());
            renderContext.setWorkspace(mainNode.getSession().getWorkspace().getName());

            JCRSiteNode site = mainNode.getResolveSite();
            renderContext.setSite(site);

            response.setCharacterEncoding(SettingsBean.getInstance().getCharacterEncoding());
            renderingResult = renderService.render(r, renderContext);
            if (!renderingResult.contains(testText)) {
                return new ProbeStatus(MessageFormat.format("Rendering Chain test result should have contained {0} but was {1}", testText, renderingResult), ProbeStatus.Health.RED);
            }
        } catch (Exception e) {
            return new ProbeStatus(MessageFormat.format("Rendering Chain test returns an error: {0}", e.getMessage()), ProbeStatus.Health.RED);
        }
        logger.debug("Rendering result: {}", renderingResult);

        return new ProbeStatus(MessageFormat.format("Rendering Chain works properly: {0}", renderingResult), ProbeStatus.Health.GREEN);
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.CRITICAL;
    }

    @Override
    public boolean needsHttpContext() {
        return true;
    }

    @Override
    public ProbeStatus getStatus() {
        return new ProbeStatus("Should be called with HTTP Context", ProbeStatus.Health.YELLOW);
    }
}
