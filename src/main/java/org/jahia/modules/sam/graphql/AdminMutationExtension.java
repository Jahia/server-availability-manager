package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import org.jahia.modules.graphql.provider.dxm.admin.GqlAdminMutation;

/**
 * Admin mutation extension
 */
@GraphQLTypeExtension(GqlAdminMutation.class)
public class AdminMutationExtension {

    private AdminMutationExtension() {
    }

    /**
     * Get server availability mutations
     * @return Server availability mutations
     */
    @GraphQLField
    @GraphQLDescription("Get server availability mutations")
    public static ServerAvailabilityMutation getServerAvailability() {
        return new ServerAvailabilityMutation();
    }


}
