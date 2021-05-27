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
import java.util.EnumSet;
import java.util.stream.Stream;

@Component(immediate = true, service = TasksIdentificationService.class)
public class TasksIdentificationServiceImpl implements TasksIdentificationService {
    private static final Logger logger = LoggerFactory.getLogger(TasksIdentificationServiceImpl.class);

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
    public Stream<TaskDetails> getTasksStream() {
        try {
            Stream<TaskDetails> backgroundJobsData = schedulerService.getAllActiveJobs()
                    .stream()
                    .map(jobDetail -> new TaskDetails(jobDetail.getName(), jobDetail.getGroup()));
            logger.warn(threadDump(true, true));

            Stream<TaskDetails> modulesData = templateManagerService.getModuleStates()
                    .entrySet()
                    .stream()
                    .filter(entry -> EnumSet.of(ModuleState.State.SPRING_STARTING, ModuleState.State.STARTING, ModuleState.State.STOPPING, ModuleState.State.WAITING_TO_BE_IMPORTED)
                            .contains(entry.getValue().getState()))
                    .map(entry -> new TaskDetails(entry.getKey().getSymbolicName(), entry.getKey().getLocation()));
            return Stream.concat(backgroundJobsData, modulesData);
        } catch (SchedulerException e) {
            logger.error("Something went wrong");
        }
        return null;
    }

    private static String threadDump(boolean lockedMonitors, boolean lockedSynchronizers) {
        StringBuffer threadDump = new StringBuffer(System.lineSeparator());
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        for(ThreadInfo threadInfo : threadMXBean.dumpAllThreads(lockedMonitors, lockedSynchronizers)) {
            if (!threadInfo.isSuspended()) {
                threadDump.append(threadInfo.toString());
            }
        }
        return threadDump.toString();
    }
}
