package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import org.jahia.modules.graphql.provider.dxm.osgi.annotations.GraphQLOsgiService;
import org.jahia.modules.sam.load.LoadAverageService;
import org.jahia.modules.sam.load.provider.JCRNodeCacheLoadAverage;
import org.jahia.modules.sam.load.provider.JCRSessionLoadAverage;
import org.jahia.modules.sam.load.provider.RequestLoadAverage;
import org.jahia.modules.sam.load.provider.ThreadLoadAverage;

import javax.inject.Inject;

@GraphQLDescription("Server load")
public class Load {

    @Inject
    @GraphQLOsgiService(service = LoadAverageService.class)
    private LoadAverageService LoadAverageService;

    @GraphQLField
    @GraphQLDescription("Get Thread load")
    public LoadProvider getThread() {
        return new LoadProvider(LoadAverageService.findProvider(ThreadLoadAverage.class.getName()).orElse(null));
    }

    @GraphQLField
    @GraphQLDescription("Get requests load")
    public LoadProvider getRequests() {
        return new LoadProvider(LoadAverageService.findProvider(RequestLoadAverage.class.getName()).orElse(null));
    }

    @GraphQLField
    @GraphQLDescription("Get JCR Sessions load")
    public LoadProvider getSessions() {
        return new LoadProvider(LoadAverageService.findProvider(JCRSessionLoadAverage.class.getName()).orElse(null));
    }

    @GraphQLField
    @GraphQLDescription("Get JCR Node Cache load across active sessions")
    public LoadProvider getCachedNodes() {
        return new LoadProvider(LoadAverageService.findProvider(JCRNodeCacheLoadAverage.class.getName()).orElse(null));
    }
}
