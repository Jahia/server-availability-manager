package org.jahia.modules.sam.core;

import org.jahia.modules.sam.TaskRegistryService;
import org.jahia.modules.sam.TasksIdentificationService;
import org.jahia.modules.sam.model.TaskDetails;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@Component(immediate = true, service = TasksIdentificationService.class)
public class TasksIdentificationServiceImpl implements TasksIdentificationService {
    private enum ThreadDumpTask {
        IMPORT_ZIP("org.jahia.services.importexport.ImportExportBaseService.importZip"),
        IMPORT_XML("org.jahia.services.importexport.ImportExportBaseService.importXML"),
        BACKGROUND_JOB("org.jahia.services.scheduler.BackgroundJob.execute"),
        BUNDLE_START("org.jahia.services.modulemanager.impl.ModuleManagerImpl.start"),
        BUNDLE_INSTALL("org.jahia.services.modulemanager.impl.ModuleManagerImpl.install");

        private final String fullSignature;

        public String getFullSignature() {
            return fullSignature;
        }

        ThreadDumpTask(String fullSignature) {
            this.fullSignature = fullSignature;
        }
    }


    private final Map<String, ThreadDumpTask> taskSignatures = EnumSet.allOf(ThreadDumpTask.class).stream()
            .collect(toMap(ThreadDumpTask::getFullSignature, threadDumpTask -> threadDumpTask));

    private TaskRegistryService taskRegistryService;

    @Reference
    public void setTaskRegistryService(TaskRegistryService taskRegistryService) {
        this.taskRegistryService = taskRegistryService;
    }

    @Override
    public Stream<TaskDetails> getRunningTasksStream() {
        List<TaskDetails> runningTasksStream = new ArrayList<>();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        for(ThreadInfo threadInfo : threadMXBean.dumpAllThreads(true, true)) {
            StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
            processStackTrace(runningTasksStream, stackTraceElements);
        }
        return Stream.of(runningTasksStream.stream(), taskRegistryService.getRegisteredTasks())
                .reduce(Stream::concat)
                .orElseGet(Stream::empty);
    }

    private void processStackTrace(List<TaskDetails> tasksToCheck, StackTraceElement[] stackTraceElements) {
        for (int i = stackTraceElements.length - 1; i >= 0; i--) {
            String fullSignature = stackTraceElements[i].getClassName() + "." + stackTraceElements[i].getMethodName();
            if (taskSignatures.containsKey(fullSignature)) {
                tasksToCheck.add(new TaskDetails("core", taskSignatures.get(fullSignature).toString()));
                break;
            }
        }
    }
}
