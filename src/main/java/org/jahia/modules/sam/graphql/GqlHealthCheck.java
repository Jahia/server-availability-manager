package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.schema.DataFetchingEnvironment;
import org.jahia.modules.graphql.provider.dxm.osgi.annotations.GraphQLOsgiService;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.healthcheck.ProbesRegistry;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@GraphQLDescription("Server healthCheck")
public class GqlHealthCheck {

    private ProbesRegistry probesRegistry;
    private final GqlProbeSeverity severityThreshold;
    private final Collection<String> includes;

    public GqlHealthCheck(GqlProbeSeverity severityThreshold, Collection<String> includes) {
        this.includes = includes;
        if (severityThreshold != null) {
            this.severityThreshold = severityThreshold;
        } else {
            this.severityThreshold = GqlProbeSeverity.MEDIUM;
        }
    }

    @Inject
    @GraphQLOsgiService
    public void setProbesRegistry(ProbesRegistry probesRegistry) {
        this.probesRegistry = probesRegistry;
    }

    @GraphQLField
    @GraphQLDescription("Highest reported status across all probes")
    public GqlProbeStatus getStatus(DataFetchingEnvironment environment) {
        Function<GqlProbeStatus, Integer> keyExtractor = (GqlProbeStatus status) -> status.getHealth().ordinal();
        return getProbes()
                .stream().map(gqlProbe -> gqlProbe.getStatus(environment))
                .filter(status -> !status.getHealth().equals(GqlProbeStatus.GqlProbeHealth.GREEN))
                .max(Comparator.comparing(keyExtractor))
                .orElse(new GqlProbeStatus("All probes are healthy", GqlProbeStatus.GqlProbeHealth.GREEN));
    }

    @GraphQLField
    @GraphQLDescription("Probes registered in SAM for the requested severity")
    public List<GqlProbe> getProbes() {
        return probesRegistry.getProbes().stream()
                .filter(p -> includes == null || includes.contains(p.getName()))
                .filter(p -> probesRegistry.getProbeSeverity(p).ordinal() >= ProbeSeverity.valueOf(severityThreshold.name()).ordinal())
                .map(GqlProbe::new).collect(Collectors.toList());
    }
}
