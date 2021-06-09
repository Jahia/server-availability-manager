package org.jahia.modules.sam.core;

import org.jahia.modules.sam.TaskRegistryService;
import org.jahia.modules.sam.TasksIdentificationService;
import org.jahia.modules.sam.model.TaskDetails;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.IntStream;
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
        return Stream.of(getDetectedTasks(), taskRegistryService.getRegisteredTasks())
                .reduce(Stream::concat)
                .orElseGet(Stream::empty);
    }

    private Stream<TaskDetails> getDetectedTasks() {
        return Arrays.stream(ManagementFactory.getThreadMXBean().dumpAllThreads(false, false))
                .map(ThreadInfo::getStackTrace)
                .flatMap(this::processStackTrace);
    }

    private Stream<TaskDetails> processStackTrace(StackTraceElement[] stackTraceElements) {
        return IntStream.rangeClosed(1, stackTraceElements.length).mapToObj(i -> stackTraceElements[stackTraceElements.length - i])
                .map(element -> element.getClassName() + "." + element.getMethodName())
                .filter(taskSignatures::containsKey).limit(1)
                .map(key -> new TaskDetails("core", taskSignatures.get(key).toString()));
    }
}
