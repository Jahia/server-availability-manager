package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import org.jahia.modules.sam.model.TaskDetails;
import org.joda.time.DateTime;

@GraphQLName("task")
@GraphQLDescription("Task that prevents server from shutdown")
public class GqlTask {
    private final TaskDetails taskDetails;

    public GqlTask(TaskDetails taskDetails) {
        this.taskDetails = taskDetails;
    }

    @GraphQLField
    @GraphQLDescription("The name of the task associated with the service")
    public String getName() {
        return taskDetails.getName();
    }

    @GraphQLField
    @GraphQLDescription("Service attached to the task being monitored")
    public String getService() {
        return taskDetails.getService();
    }

    @GraphQLField
    @GraphQLDescription("Datetime at which the task was started or registered")
    public String getStarted() {
        return taskDetails.getStarted() != null ? (new DateTime(taskDetails.getStarted().getTime().getTime())).toString() : null;
    }
}
