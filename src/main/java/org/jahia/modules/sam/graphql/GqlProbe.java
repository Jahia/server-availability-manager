package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLField;
import org.jahia.modules.graphql.provider.dxm.osgi.annotations.GraphQLOsgiService;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.healthcheck.ProbesRegistry;

import javax.inject.Inject;

public class GqlProbe {

    private Probe probe;
    private ProbesRegistry probesRegistry;

    @Inject
    @GraphQLOsgiService
    public void setProbesRegistry(ProbesRegistry probesRegistry) {
        this.probesRegistry = probesRegistry;
    }

    public GqlProbe(Probe probe) {
        this.probe = probe;
    }

    @GraphQLField
    public String getName() {
        return probe.getName();
    }

    @GraphQLField
    public String getDescription() {
        return probe.getDescription();
    }

    @GraphQLField
    public GqlProbeSeverity getSeverity() {
        return GqlProbeSeverity.valueOf(probesRegistry.getProbeSeverity(probe).name());
    }

    @GraphQLField
    public GqlProbeStatus getStatus() {
        return GqlProbeStatus.valueOf(probe.getStatus().name());
    }
}
