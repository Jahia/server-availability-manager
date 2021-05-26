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
package org.jahia.modules.serveravailability.core;

/**
 * Task bean
 */
public interface Task {
    /**
     * Get Service attached to the task being monitored
     *
     * @return the key
     */
    String getService();

    void setService(String service);

    /**
     * Get the name of the task associated with the service
     *
     * @return name
     */
    String getTaskName();

    void setTaskName(String taskName);

    /**
     * Get When did the task start
     *
     * @return digested secret
     */
    String getStarted();

    void setStarted(String started);
}
