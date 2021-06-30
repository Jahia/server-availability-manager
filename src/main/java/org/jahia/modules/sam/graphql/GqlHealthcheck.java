package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.healthcheck.ProbesRegistry;
import org.jahia.osgi.BundleUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@GraphQLDescription("Server healthcheck")
public class GqlHealthcheck {

    private ProbesRegistry probesRegistry = BundleUtils.getOsgiService(ProbesRegistry.class, null);
    private GqlProbeSeverity severityThreshold;

    public GqlHealthcheck(GqlProbeSeverity severityThreshold) {
        if (severityThreshold != null) {
            this.severityThreshold = severityThreshold;
        } else {
            this.severityThreshold = GqlProbeSeverity.MEDIUM;
        }

    }

    @GraphQLField
    @GraphQLDescription("Highest reported status across all probes")
    public GqlProbeStatus getStatus() {
        return getProbes().stream().map(GqlProbe::getStatus).max(Comparator.comparing(Enum::ordinal)).orElse(GqlProbeStatus.GREEN);
    }

    @GraphQLField
    public List<GqlProbe> getProbes() {
        return probesRegistry.getProbes().stream()
                .filter(p -> probesRegistry.getProbeSeverity(p).ordinal() >= ProbeSeverity.valueOf(severityThreshold.name()).ordinal())
                .map(GqlProbe::new).collect(Collectors.toList());
    }
}
