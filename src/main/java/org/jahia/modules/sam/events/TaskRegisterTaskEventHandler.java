package org.jahia.modules.sam.events;

import org.jahia.modules.sam.TaskRegistryService;
import org.jahia.modules.sam.model.TaskDetails;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

@Component(service = EventHandler.class, immediate = true, property = EventConstants.EVENT_TOPIC + "=org/jahia/module/sam/register")
public class TaskRegisterTaskEventHandler implements EventHandler {

    private static final Logger logger = LoggerFactory.getLogger(TaskRegisterTaskEventHandler.class);

    private static final String SERVICE_PROPERTY = "service";
    public static final String NAME_PROPERTY = "name";
    private static final String STARTED_PROPERTY = "started";
    private static final String ACTION_PROPERTY = "action";

    private TaskRegistryService taskRegistryService;

    @Reference
    public void setTaskRegistryService(TaskRegistryService taskRegistryService) {
        this.taskRegistryService = taskRegistryService;
    }

    @Override
    public void handleEvent(Event event) {
        TaskDetails taskInfo = new TaskDetails(event.getProperty(SERVICE_PROPERTY).toString(), event.getProperty(NAME_PROPERTY).toString());
        taskInfo.setStarted(event.getProperty(STARTED_PROPERTY) == null ? null : (Calendar) event.getProperty(STARTED_PROPERTY));
        if ("REGISTER".equals(event.getProperty(ACTION_PROPERTY))) {
            taskRegistryService.registerTask(taskInfo);
        } else {
            taskRegistryService.unregisterTask(taskInfo.getName());
        }
        taskRegistryService.registerTask(taskInfo);
        logger.info("Task is registered with name: {} and service: {}", event.getProperty(NAME_PROPERTY), event.getProperty(SERVICE_PROPERTY));
    }
}
