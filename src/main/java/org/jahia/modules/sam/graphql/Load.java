package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import org.jahia.utils.JCRNodeCacheLoadAverage;
import org.jahia.utils.JCRSessionLoadAverage;
import org.jahia.utils.RequestLoadAverage;

@GraphQLDescription("Server load")
public class Load {

    @GraphQLField
    @GraphQLDescription("Get requests load")
    public LoadValue getRequests() {
        return new LoadValue(RequestLoadAverage.getInstance());
    }

    @GraphQLField
    @GraphQLDescription("Get JCR Sessions load")
    public LoadValue getSessions() {
        return new LoadValue(JCRSessionLoadAverage.getInstance());
    }

    @GraphQLField
    @GraphQLDescription("Get JCR Node Cache load across active sessions")
    public LoadValue getCachedNodes() {
        return new LoadValue(JCRNodeCacheLoadAverage.getInstance());
    }
}
