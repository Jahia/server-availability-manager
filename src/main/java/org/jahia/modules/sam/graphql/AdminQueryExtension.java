package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import org.jahia.modules.graphql.provider.dxm.admin.GqlAdminQuery;

@GraphQLTypeExtension(GqlAdminQuery.class)
public class AdminQueryExtension {

    @GraphQLField
    @GraphQLName("serverAvailabilityManager")
    @GraphQLDescription("SAM queries for the tasks")
    public static ServerAvailabilityQuery serverAvailabilityManager() {
        return new ServerAvailabilityQuery();
    }
}
