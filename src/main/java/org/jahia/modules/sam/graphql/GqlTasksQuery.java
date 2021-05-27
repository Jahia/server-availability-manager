package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import org.jahia.modules.graphql.provider.dxm.admin.GqlAdminQuery;
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
    @GraphQLDescription("Server availability manager queries")
    public List<GqlTask> tasks() {
        return tasksIdentificationService.getTasksStream().map(GqlTask::new).collect(Collectors.toList());
    }
}
