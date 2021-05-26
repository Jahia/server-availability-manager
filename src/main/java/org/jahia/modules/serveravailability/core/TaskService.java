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

import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.RepositoryException;
import java.util.stream.Stream;

/**
 * Service to handle tasks
 */
public interface TaskService {
    /**
     * You'll have to call create() on builder and save the session afterwards.
     *
     * @param task Task
     * @return The task builder
     * @throws javax.jcr.RepositoryException when repository operation fails
     */
    public TaskBuilder taskBuilder(Task task, JCRSessionWrapper session) throws RepositoryException;


    /**
     * Get the task  for the specified key, or null if it does not exist
     *
     * @param taskName     The task name
     * @param session The session to use to retrieve the task
     * @return Task
     * @throws javax.jcr.RepositoryException when repository operation fails
     */
    public Task getTask(String taskName, JCRSessionWrapper session) throws RepositoryException;

    /**
     * Get all tasks
     *
     * @param session  The session to use to retrieve the tasks
     * @return The tasks
     * @throws javax.jcr.RepositoryException when repository operation fails
     */
    public Stream<Task> getTasks(JCRSessionWrapper session) throws RepositoryException;

    /**
     * Update task. You can change service, taskName
     *
     * @param task The updated task
     * @param session The session
     * @return true if operation succeeds, false if task does not exist
     * @throws javax.jcr.RepositoryException when repository operation fails
     */
    public boolean updateTask(Task task, JCRSessionWrapper session) throws RepositoryException;

    /**
     * Delete an existing task
     *
     * @param taskName     The taskName
     * @param session The session
     * @return true if operation succeeds, false if task does not exist
     * @throws javax.jcr.RepositoryException when repository operation fails
     */
    public boolean deleteTask(String taskName, JCRSessionWrapper session) throws RepositoryException;


}
