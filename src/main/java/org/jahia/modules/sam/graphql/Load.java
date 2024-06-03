package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import org.jahia.utils.load.*;

@GraphQLDescription("Server load")
public class Load {

    @GraphQLField
    @GraphQLDescription("Get Thread load")
    public LoadProvider getThread() {
        return new LoadProvider(LoadAverageMonitor.getInstance().findProviderForName(ThreadLoadAverage.NAME).orElse(null));
    }

    @GraphQLField
    @GraphQLDescription("Get requests load")
    public LoadProvider getRequests() {
        return new LoadProvider(LoadAverageMonitor.getInstance().findProviderForName(RequestLoadAverage.NAME).orElse(null));
    }

    @GraphQLField
    @GraphQLDescription("Get JCR Sessions load")
    public LoadProvider getSessions() {
        return new LoadProvider(LoadAverageMonitor.getInstance().findProviderForName(JCRSessionLoadAverage.NAME).orElse(null));
    }

    @GraphQLField
    @GraphQLDescription("Get JCR Node Cache load across active sessions")
    public LoadProvider getCachedNodes() {
        return new LoadProvider(LoadAverageMonitor.getInstance().findProviderForName(JCRNodeCacheLoadAverage.NAME).orElse(null));
    }
}
