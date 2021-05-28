package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import org.jahia.modules.graphql.provider.dxm.osgi.annotations.GraphQLOsgiService;
import org.jahia.modules.sam.TasksIdentificationService;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@GraphQLName("TasksQuery")
@GraphQLDescription("Queries for Tasks in Server availability manager")
public class GqlTasksQuery {

    @Inject
    @GraphQLOsgiService
    private TasksIdentificationService tasksIdentificationService;


    @GraphQLField
    @GraphQLName("tasks")
    @GraphQLDescription("Lists tasks running on the Jahia server. A server should not be stopped/restarted when any of these tasks are present. These tasks are specific to the server being queried and are not shared accorss a cluster")
    public List<GqlTask> tasks() {
        return tasksIdentificationService.getRunningTasksStream().map(GqlTask::new).collect(Collectors.toList());
    }
}