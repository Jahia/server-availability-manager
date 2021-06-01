package org.jahia.modules.sam.events;

import org.jahia.modules.sam.TaskRegistryService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jahia.modules.sam.events.TaskRegisterTaskEventHandler.NAME_PROPERTY;

@Component(service = EventHandler.class, immediate = true, property = EventConstants.EVENT_TOPIC+"=org/jahia/module/sam/unregister")
public class TaskUnregisterTaskEventHandler implements EventHandler {

    private static final Logger logger = LoggerFactory.getLogger(TaskUnregisterTaskEventHandler.class);

    private TaskRegistryService taskRegistryService;

    @Reference
    public void setTaskRegistryService(TaskRegistryService taskRegistryService) {
        this.taskRegistryService = taskRegistryService;
    }

    @Override
    public void handleEvent(Event event) {
        taskRegistryService.unregisterTask(event.getProperty(NAME_PROPERTY).toString());
        logger.info("Task is unregistered with name: {}", event.getProperty(NAME_PROPERTY));
    }
}
