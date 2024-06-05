package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.*;
import org.jahia.modules.graphql.provider.dxm.admin.GqlJahiaAdminQuery;
import org.jahia.modules.sam.TasksIdentificationService;
import org.jahia.osgi.BundleUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@GraphQLDescription("Queries for Tasks in Server availability manager")
@GraphQLTypeExtension(GqlJahiaAdminQuery.class)
public class ServerAvailabilityQuery {

    public ServerAvailabilityQuery(GqlJahiaAdminQuery admin) {
        this.tasksIdentificationService = BundleUtils.getOsgiService(TasksIdentificationService.class, null);
    }

    private TasksIdentificationService tasksIdentificationService;

    @GraphQLField
    @GraphQLDescription("Lists tasks running on the Jahia server. A server should not be stopped/restarted when any of these tasks are present. These tasks are specific to the server being queried and are not shared accorss a cluster")
    public List<GqlTask> getTasks() {
        return tasksIdentificationService.getRunningTasksStream().map(GqlTask::new).collect(Collectors.toList());
    }

    @GraphQLField
    @GraphQLDescription("HealthCheck node")
    public GqlHealthCheck getHealthCheck(
            @GraphQLName("severity") @GraphQLDescription("Returns SAM probes with this severity or higher") GqlProbeSeverity severity,
            @GraphQLName("includes") @GraphQLDescription("Returns only SAM probes with probe names included in this list") Collection<@GraphQLNonNull String> includes) {
        return new GqlHealthCheck(severity, includes);
    }

    @GraphQLField
    @GraphQLDescription("Get server load")
    public Load load() {
        return new Load();
    }
}
