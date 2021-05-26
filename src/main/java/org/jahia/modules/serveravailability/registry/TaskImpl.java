/*
 * Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jahia.modules.serveravailability.registry;

import org.jahia.modules.serveravailability.core.Task;

import java.util.Calendar;

/**
 * Implementation for Token details
 */
public class TaskImpl implements Task {
    private String service;

    private String taskName;

    private String started;

    /**
     * New task
     *
     * @param service  service
     * @param taskName taskName
     * @param started  started
     */
    TaskImpl(String service, String taskName, String started) {
        this.service = service;
        this.taskName = taskName;
        this.started = started;
    }

    @Override
    public String getService() {
        return service;
    }

    @Override
    public void setService(String service) {
        this.service = service;
    }

    @Override
    public String getTaskName() {
        return taskName;
    }

    @Override
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    @Override
    public String getStarted() {
        return started;
    }

    @Override
    public void setStarted(String started) {
        this.started = started;
    }

    @Override
    public String toString() {
        return "TaskImpl{" +
                "service='" + service + '\'' +
                ", taskName='" + taskName + '\'' +
                ", started='" + started + '\'' +
                '}';
    }
}
