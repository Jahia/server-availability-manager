package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.*;
import org.jahia.modules.graphql.provider.dxm.DataFetchingException;
import org.jahia.modules.graphql.provider.dxm.admin.GqlJahiaAdminMutation;
import org.jahia.modules.sam.TaskRegistryService;
import org.jahia.modules.sam.TasksIdentificationService;
import org.jahia.modules.sam.model.TaskDetails;
import org.jahia.osgi.BundleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.util.Calendar;

/**
 * Server availability mutations
 */
@GraphQLTypeExtension(GqlJahiaAdminMutation.class)
public class ServerAvailabilityMutation {
    private static final Logger logger = LoggerFactory.getLogger(ServerAvailabilityMutation.class);

    public ServerAvailabilityMutation(GqlJahiaAdminMutation admin) {
        this.taskRegistryService = BundleUtils.getOsgiService(TaskRegistryService.class, null);
        this.tasksIdentificationService = BundleUtils.getOsgiService(TasksIdentificationService.class, null);
    }

    private TaskRegistryService taskRegistryService;

    private TasksIdentificationService tasksIdentificationService;

    /**
     * Register a task
     *
     * @param service Service attached to the task being monitored
     * @param name The name of the task associated with the service
     * @throws Exception
     */
    @GraphQLField
    @GraphQLDescription("Create a task")
    public boolean createTask(@GraphQLName("service") @GraphQLDescription("Service name") @GraphQLNonNull String service,
                              @GraphQLName("name") @GraphQLDescription("Task name") @GraphQLNonNull String name) {
        try {

            //Check if it's alphanumerical + '-' and '_' with a limited length (100 Chars)
            if(!service.matches("[a-zA-Z0-9-_]{1,50}")) {
                throw new DataFetchingException("Service is not a alphanumerical with a limited length of 50 characters");
            }

            //Creating task
            TaskDetails taskDetails = new TaskDetails(service,name);
            taskDetails.setStarted(Calendar.getInstance()); //Setting started date to the time the task is being created
            taskRegistryService.registerTask(taskDetails);

            return true;
        } catch (Exception e) {
            logger.error("Can't create a task: {}", e.getMessage());
            throw new DataFetchingException(e);
        }
    }

    /**
     * Delete a task
     *
     * @param name The name of the task associated with the service
     * @return false return means the task was not found
     * @throws Exception
     */
    @GraphQLField
    @GraphQLDescription("Delete a task")
    public boolean deleteTask(@GraphQLName("service") @GraphQLDescription("Service name") @GraphQLNonNull String service,
                              @GraphQLName("name") @GraphQLDescription("Task name") @GraphQLNonNull String name) {
        try {

            //Check if taskDetail is registered
            if (taskRegistryService.getRegisteredTasks().noneMatch(task -> task.equals(new TaskDetails(service, name)))) {
                return false;
            }

            TaskDetails taskDetails = new TaskDetails(service, name);
            taskRegistryService.unregisterTask(taskDetails);

            return true;
        } catch (Exception e) {
            logger.error("Can't delete task: {}", e.getMessage());
            throw new DataFetchingException(e);
        }
    }

    /**
     * Shutdown the server
     * @param timeout In seconds, maximum time to wait for server to be ready to shutdown
     * @param force Force immediate shutdown even if server is busy
     * @param dryRun Do not send the shutdown event
     * @return
     * @throws Exception
     */
    @GraphQLField
    @GraphQLDescription("Shutdown the server")
    public boolean shutdown(@GraphQLName("timeout") @GraphQLDescription("In seconds, maximum time to wait for server to be ready to shutdown") Integer timeout,
                            @GraphQLName("force") @GraphQLDescription("Force immediate shutdown even if server is busy") Boolean force,
                            @GraphQLName("dryRun") @GraphQLDescription("Do not send the shutdown event") Boolean dryRun) throws DataFetchingException {
        try {
            if (force != null && force) {
                return doShutdown(dryRun != null && dryRun);
            } else {
                if (timeout == null) {
                    timeout = 25;
                }
                long timeoutInstant = System.currentTimeMillis() + (timeout * 1000);
                while (System.currentTimeMillis() < timeoutInstant) {
                    if (!tasksIdentificationService.getRunningTasksStream().findFirst().isPresent()) {
                        return doShutdown(dryRun != null && dryRun);
                    }
                    sleep();
                }
            }
        } catch (Exception e) {
            throw new DataFetchingException(e);
        }
        return false;
    }

    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean doShutdown(boolean dryRun) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, MalformedObjectNameException, IOException {
        Integer port = (Integer) ManagementFactory.getPlatformMBeanServer().getAttribute(new ObjectName("Catalina:type=Server"), "port");

        logger.info("Sending shutdown signal to port {}", port);

        if (!dryRun) {
            try (Socket echoSocket = new Socket("localhost", port)) {
                PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                out.println("SHUTDOWN");
            }
        }
        return true;
    }

}
