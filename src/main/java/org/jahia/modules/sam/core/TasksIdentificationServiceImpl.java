package org.jahia.modules.sam.core;

import org.jahia.data.templates.ModuleState;
import org.jahia.modules.sam.TasksIdentificationService;
import org.jahia.modules.sam.model.TaskDetails;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component(immediate = true, service = TasksIdentificationService.class)
public class TasksIdentificationServiceImpl implements TasksIdentificationService {
    private static final Logger logger = LoggerFactory.getLogger(TasksIdentificationServiceImpl.class);
    private static final Pattern THREAD_DUMP_PATTERN = Pattern.compile("importSiteZip|importContent");

    private SchedulerService schedulerService;

    private JahiaTemplateManagerService templateManagerService;

    @Reference
    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @Reference
    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    @Override
    public Stream<TaskDetails> getRunningTasksStream() {
        try {
            Stream<TaskDetails> backgroundJobsData = schedulerService.getAllActiveJobs()
                    .stream()
                    .map(jobDetail -> new TaskDetails(jobDetail.getName(), jobDetail.getGroup()));

            Stream<TaskDetails> modulesData = templateManagerService.getModuleStates()
                    .entrySet()
                    .stream()
                    .filter(entry -> EnumSet.of(ModuleState.State.SPRING_STARTING, ModuleState.State.STARTING, ModuleState.State.STOPPING, ModuleState.State.WAITING_TO_BE_IMPORTED)
                            .contains(entry.getValue().getState()))
                    .map(entry -> new TaskDetails(entry.getKey().getSymbolicName(), entry.getKey().getLocation()));

            return Stream.of(backgroundJobsData, modulesData, getTasksFromThreadDump())
                    .reduce(Stream::concat)
                    .orElseGet(Stream::empty);
        } catch (SchedulerException e) {
            logger.error("Can't get data from the scheduler service: {}", e.getMessage());
        }
        return null;
    }

    private Stream<TaskDetails> getTasksFromThreadDump() {
        List<TaskDetails> tasksToCheck = new ArrayList<>();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        for(ThreadInfo threadInfo : threadMXBean.dumpAllThreads(true, true)) {
           StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                if (THREAD_DUMP_PATTERN.matcher(stackTraceElement.getMethodName()).find()) {
                    tasksToCheck.add(new TaskDetails(stackTraceElement.getMethodName(), stackTraceElement.getClassName()));
                }
            }

        }
        return tasksToCheck.stream();
    }
}
