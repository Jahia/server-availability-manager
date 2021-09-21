package org.jahia.modules.sam;

/**
 * Probe health
 */
public class ProbeStatus {
    public enum Health {
        GREEN, YELLOW, RED
    }

    private String message;
    private Health health;

    public ProbeStatus(String message, Health health) {
        this.message = message;
        this.health = health;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Health getHealth() {
        return health;
    }

    public void setHealth(Health health) {
        this.health = health;
    }
}
