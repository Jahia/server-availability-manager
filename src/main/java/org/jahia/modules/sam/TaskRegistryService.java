package org.jahia.modules.sam;

import org.jahia.modules.sam.model.TaskDetails;

import java.util.stream.Stream;

public interface TaskRegistryService {

    void registerTask(TaskDetails taskDetails);

    void unregisterTask(String name);

    Stream<TaskDetails> getRegisteredTasks();
}
