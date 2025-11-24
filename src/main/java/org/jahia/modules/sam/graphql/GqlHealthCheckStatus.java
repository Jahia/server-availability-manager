package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDeprecate;
import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;

@GraphQLDescription("HealthCheck Status")
public class GqlHealthCheckStatus {
    
    private String message;
    private GqlProbeHealth gqlProbeHealth;

    public GqlHealthCheckStatus(String message, GqlProbeHealth gqlProbeHealth) {
        this.message = message;
        this.gqlProbeHealth = gqlProbeHealth;
    }

    @GraphQLField
    @GraphQLDescription("Message explaining probe status")
    @GraphQLDeprecate("When multiple probe return the same error status (YELLOW or RED), the message does not guarantee which of the probe will get its message returned. We recommend using the \"health\" parameter and corresponding individual probe message instead.")
    public String getMessage() {
        return message;
    }

    @GraphQLField
    @GraphQLDescription("Health of the probe")
    public GqlProbeHealth getHealth() {
        return gqlProbeHealth;
    }
}
