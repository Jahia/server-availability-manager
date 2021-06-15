package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import org.jahia.modules.graphql.provider.dxm.admin.GqlJahiaAdminQuery;
import org.jahia.modules.sam.TasksIdentificationService;
import org.jahia.osgi.BundleUtils;

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
    @GraphQLDescription("Healthcheck node")
    public GqlHealthcheck getHealthCheck(@GraphQLName("severity") GqlProbeSeverity severity) {
        return new GqlHealthcheck(severity);
    }

    @GraphQLField
    @GraphQLDescription("Get server load")
    public Load load() {
        return new Load();
    }
}
