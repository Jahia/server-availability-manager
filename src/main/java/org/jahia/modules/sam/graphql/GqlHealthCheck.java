package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import org.jahia.modules.graphql.provider.dxm.DataFetchingException;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.healthcheck.ProbesRegistry;
import org.jahia.osgi.BundleUtils;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@GraphQLDescription("Server healthCheck")
public class GqlHealthCheck {

    private GqlProbeSeverity severityThreshold;

    public GqlHealthCheck(GqlProbeSeverity severityThreshold) {
        if (severityThreshold != null) {
            this.severityThreshold = severityThreshold;
        } else {
            this.severityThreshold = GqlProbeSeverity.MEDIUM;
        }

    }

    @GraphQLField
    @GraphQLDescription("Highest reported status across all probes")
    public GqlProbeStatus getStatus() {
        Function<GqlProbeStatus, Integer> keyExtractor = (GqlProbeStatus status) -> status.getHealth().ordinal();
        return getProbes()
                .stream().map(GqlProbe::getStatus)
                .filter(status -> !status.getHealth().equals(GqlProbeStatus.GqlProbeHealth.GREEN))
                .max(Comparator.comparing(keyExtractor))
                .orElse(new GqlProbeStatus("All probes are healthy", GqlProbeStatus.GqlProbeHealth.GREEN));
    }

    @GraphQLField
    public List<GqlProbe> getProbes() {
        ProbesRegistry probesRegistry = BundleUtils.getOsgiService(ProbesRegistry.class, null);

        if (probesRegistry == null) {
            throw new DataFetchingException("Failed to find probe registry service");
        }

        return probesRegistry.getProbes().stream()
                .filter(p -> probesRegistry.getProbeSeverity(p).ordinal() >= ProbeSeverity.valueOf(severityThreshold.name()).ordinal())
                .map(GqlProbe::new).collect(Collectors.toList());
    }
}
