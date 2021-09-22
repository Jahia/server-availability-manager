package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import org.jahia.modules.graphql.provider.dxm.DataFetchingException;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.modules.sam.healthcheck.ProbesRegistry;
import org.jahia.osgi.BundleUtils;

public class GqlProbe {

    private Probe probe;

    public GqlProbe(Probe probe) {
        this.probe = probe;
    }

    @GraphQLField
    @GraphQLDescription("Name of the probe")
    public String getName() {
        return probe.getName();
    }

    @GraphQLField
    @GraphQLDescription("Description specified by the developer of the probe")
    public String getDescription() {
        return probe.getDescription();
    }

    @GraphQLField
    @GraphQLDescription("Severity of the probe (LOW to CRITICAL)")
    public GqlProbeSeverity getSeverity() {
        ProbesRegistry probesRegistry = BundleUtils.getOsgiService(ProbesRegistry.class, null);

        if (probesRegistry == null) {
            throw new DataFetchingException("Failed to find probe registry service");
        }

        return GqlProbeSeverity.valueOf(probesRegistry.getProbeSeverity(probe).name());
    }

    @GraphQLField
    @GraphQLDescription("Status reported by the probe (GREEN to RED)")
    public GqlProbeStatus getStatus() {
        ProbeStatus status = probe.getStatus();
        return new GqlProbeStatus(status.getMessage(), GqlProbeStatus.GqlProbeHealth.valueOf(status.getHealth().name()));
    }
}
