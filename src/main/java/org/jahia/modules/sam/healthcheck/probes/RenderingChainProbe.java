package org.jahia.modules.sam.healthcheck.probes;

import org.apache.jackrabbit.util.ISO8601;
import org.jahia.api.Constants;
import org.jahia.bin.Render;
import org.jahia.modules.graphql.provider.dxm.node.NodeHelper;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.settings.SettingsBean;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Locale;

@Component(service = Probe.class, immediate = true)
public class RenderingChainProbe implements Probe {

    private Logger logger = LoggerFactory.getLogger(RenderingChainProbe.class);

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
            JahiaSite defaultSite = jahiaSitesService.getDefaultSite();
            if (defaultSite == null) {
                return new ProbeStatus("No site installed, postponing rendering test.", ProbeStatus.Health.GREEN);
            }
            String textTest = MessageFormat.format("Rendering Chain Test done at {0}", ISO8601.format(Calendar.getInstance()));
            JCRSessionWrapper currentUserSession = sessionFactory.getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.forLanguageTag(defaultSite.getDefaultLanguage()));
            JCRNodeWrapper testNode;
            if (!currentUserSession.nodeExists("/sites/systemsite/home/renderingChainTest")) {
                JCRNodeWrapper home = currentUserSession.getNode("/sites/systemsite/home");
                testNode = home.addNode("renderingChainTest", "sam:renderingChain");
                testNode.setProperty("text", textTest);
                testNode.setProperty("jcr:title", "Rendering Chain Test Node");
                currentUserSession.save();
            } else {
                testNode = currentUserSession.getNode("/sites/systemsite/home/renderingChainTest");
                testNode.setProperty("text", textTest);
                currentUserSession.save();
            }
            JCRNodeWrapper mainNode = NodeHelper.getNodeInLanguage(testNode, "en");
            Resource r = new Resource(mainNode, "html", "default", "module");
            renderContext.setMainResource(r);
            renderContext.setServletPath(Render.getRenderServletPath());
            renderContext.setWorkspace(mainNode.getSession().getWorkspace().getName());

            JCRSiteNode site = mainNode.getResolveSite();
            renderContext.setSite(site);

            response.setCharacterEncoding(SettingsBean.getInstance().getCharacterEncoding());
            renderingResult = renderService.render(r, renderContext);
            if (!renderingResult.contains(textTest)) {
                return new ProbeStatus(MessageFormat.format("Error rendering test, result hsould have contained {0} but was {1}",textTest, renderingResult), ProbeStatus.Health.RED );
            }
        } catch (Exception e) {
            return new ProbeStatus(MessageFormat.format("Error rendering test: {0}",e.getMessage()), ProbeStatus.Health.RED);
        }
        logger.debug("Rendering result: {}", renderingResult);

        return new ProbeStatus(MessageFormat.format("All good: {0}", renderingResult), ProbeStatus.Health.GREEN);
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
