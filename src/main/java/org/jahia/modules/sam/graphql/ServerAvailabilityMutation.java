package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDefaultValue;
import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import org.jahia.modules.graphql.provider.dxm.osgi.annotations.GraphQLOsgiService;
import org.jahia.modules.sam.TasksIdentificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.management.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.Socket;

/**
 * Server availability mutations
 */
@GraphQLDescription("Server availability mutations")
public class ServerAvailabilityMutation {
    private static Logger logger = LoggerFactory.getLogger(ServerAvailabilityMutation.class);

    @Inject
    @GraphQLOsgiService
    private TasksIdentificationService tasksIdentificationService;

    /**
     * Shutdown the server
     * @param timeout Maximum time to wait for server to be ready to shutdown
     * @param force Force shutdown even if server is busy
     * @param dryRun Do not send the shutdown event
     * @return
     * @throws Exception
     */
    @GraphQLField
    @GraphQLDescription("Shutdown the server")
    public boolean shutdown(@GraphQLName("timeout") @GraphQLDescription("Maximum time to wait for server to be ready to shutdown") Integer timeout,
                            @GraphQLName("force") @GraphQLDescription("Force shutdown even if server is busy") boolean force,
                            @GraphQLName("dryRun") @GraphQLDescription("Do not send the shutdown event") boolean dryRun) throws Exception {
        if (force) {
            return doShutdown(dryRun);
        } else {
            if (timeout == null) {
                timeout = 25;
            }
            for (int i = timeout; i > 0; i--) {
                if (!tasksIdentificationService.getRunningTasksStream().findFirst().isPresent()) {
                    return doShutdown(dryRun);
                }
                Thread.sleep(1000);
            }
        }
        return false;
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
