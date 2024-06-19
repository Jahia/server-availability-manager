package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;

@GraphQLDescription("Available severity levels for SAM probes")
public enum GqlProbeSeverity {
    DEBUG, LOW, MEDIUM, HIGH, CRITICAL
}
