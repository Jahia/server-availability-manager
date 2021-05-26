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

import javax.jcr.RepositoryException;

/**
 * Task builder
 */
public interface TaskBuilder {
    /**
     * Set task if you want ot use a predefined value
     * @param task task value
     * @return builder
     */
    public TaskBuilder setTask(Task task);

    /**
     * Create the task in the JCR. Session has to be saved to persist the task.
     * @return the task value
     * @throws javax.jcr.RepositoryException if repository operation fails
     */
    public String create() throws RepositoryException;
}
