package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;

@GraphQLDescription("Probe status")
public class GqlProbeStatus {
    public enum GqlProbeHealth {
        GREEN, YELLOW, RED;
    }

    private String message;
    private GqlProbeHealth gqlProbeHealth;

    public GqlProbeStatus(String message, GqlProbeHealth gqlProbeHealth) {
        this.message = message;
        this.gqlProbeHealth = gqlProbeHealth;
    }

    @GraphQLField
    @GraphQLDescription("Message explaining probe status")
    public String getMessage() {
        return message;
    }

    @GraphQLField
    @GraphQLDescription("Health of the probe")
    public GqlProbeHealth getHealth() {
        return gqlProbeHealth;
    }
}
