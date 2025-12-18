package org.jahia.modules.sam.core;

import org.jahia.modules.sam.ProbeStatus;

/**
 * Simple utils class to manipulate probe data
 */
public final class ProbeStatusUtils {

    private ProbeStatusUtils() {
        // Utility class
    }

    /**
     * Aggregate a new message to an existing probe status
     *
     * @param status The status of a probe
     * @param message A message to aggregate with to the status
     * @param health The health level associated with the message
     */
    public static void aggregateStatus(ProbeStatus status, String message, ProbeStatus.Health health) {
        if (status.getHealth() == ProbeStatus.Health.GREEN) {
            status.setMessage(message);
            status.setHealth(health);
        } else {
            if (status.getHealth() != ProbeStatus.Health.RED && health != ProbeStatus.Health.GREEN) {
                status.setHealth(health);
            }
            status.setMessage(String.format("%s - %s", status.getMessage(), message));
        }
    }
}
