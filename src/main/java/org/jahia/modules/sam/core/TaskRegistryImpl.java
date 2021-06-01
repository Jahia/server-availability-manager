package org.jahia.modules.sam.core;

import org.jahia.modules.sam.TaskRegistryService;
import org.jahia.modules.sam.model.TaskDetails;
import org.osgi.service.component.annotations.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Component(immediate = true, service = TaskRegistryService.class)
public class TaskRegistryImpl implements TaskRegistryService {

    private final ConcurrentHashMap<TaskDetails, TaskDetails> taskDetailList = new ConcurrentHashMap<>();

    @Override
    public void registerTask(TaskDetails taskDetails) {
        taskDetailList.put(taskDetails, taskDetails);
    }

    @Override
    public void unregisterTask(TaskDetails taskDetails) {
        taskDetailList.remove(taskDetails);
    }

    @Override
    public Stream<TaskDetails> getRegisteredTasks() {
        return taskDetailList.values().stream();
    }
}
