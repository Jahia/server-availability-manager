package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.schema.DataFetchingEnvironment;
import org.jahia.modules.graphql.provider.dxm.osgi.annotations.GraphQLOsgiService;
import org.jahia.modules.graphql.provider.dxm.util.ContextUtil;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.modules.sam.healthcheck.ProbesRegistry;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@GraphQLDescription("Probes registered with SAM")
public class GqlProbe {

    private Probe probe;
    private ProbesRegistry probesRegistry;

    @Inject
    @GraphQLOsgiService
    public void setProbesRegistry(ProbesRegistry probesRegistry) {
        this.probesRegistry = probesRegistry;
    }

    public GqlProbe() {
    }

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
        return GqlProbeSeverity.valueOf(probesRegistry.getProbeSeverity(probe).name());
    }

    @GraphQLField
    @GraphQLDescription("Status reported by the probe (GREEN to RED)")
    public GqlProbeStatus getStatus(DataFetchingEnvironment environment) {
        ProbeStatus status;
        if (probe.needsHttpContext()) {
            HttpServletRequest request = ContextUtil.getHttpServletRequest(environment.getGraphQlContext());
            HttpServletResponse response = ContextUtil.getHttpServletResponse(environment.getGraphQlContext());
            if (request == null || response == null) {
                throw new RuntimeException("No HttpRequest or HttpResponse");
            }
            status = probe.getStatus(request, response);
        } else {
            status = probe.getStatus();
        }
        return new GqlProbeStatus(status.getMessage(),
                GqlProbeStatus.GqlProbeHealth.valueOf(status.getHealth().name()));
    }
}
