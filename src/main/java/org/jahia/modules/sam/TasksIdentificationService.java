package org.jahia.modules.sam;

import org.jahia.modules.sam.model.TaskDetails;

import java.util.stream.Stream;

public interface TasksIdentificationService {
    Stream<TaskDetails> getTasksStream();
}
