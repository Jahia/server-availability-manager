package org.jahia.modules.sam.model;

import java.util.Calendar;

public class TaskDetails {
    private String name;
    private String service;
    private Calendar started;

    public TaskDetails(String name, String service) {
        this.name = name;
        this.service = service;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Calendar getStarted() {
        return started;
    }

    public void setStarted(Calendar started) {
        this.started = started;
    }
}
