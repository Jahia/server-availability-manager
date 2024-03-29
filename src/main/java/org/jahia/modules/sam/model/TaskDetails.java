package org.jahia.modules.sam.model;

import java.util.Calendar;
import java.util.Objects;

public class TaskDetails {
    private String service;
    private String name;
    private Calendar started;

    public TaskDetails(String service, String name) {
        this.service = service;
        this.name = name;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Calendar getStarted() {
        return started;
    }

    public void setStarted(Calendar started) {
        this.started = started;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskDetails that = (TaskDetails) o;
        return service.equals(that.service) && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(service, name);
    }
}
