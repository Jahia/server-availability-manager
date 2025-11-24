package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;

@GraphQLDescription("Available health statuses for a probe")
public enum GqlProbeHealth {
    GREEN, YELLOW, RED;
}
