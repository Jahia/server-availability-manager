/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.sam.load;

import org.jahia.services.observation.JahiaEventService;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component(immediate = true, service = LoadAverageService.class)
public class LoadAverageServiceImpl implements LoadAverageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadAverageService.class);
    private static final ScheduledExecutorService executor =  Executors.newSingleThreadScheduledExecutor();
    @Reference(service = LoadAverageProvider.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
            bind = "addProvider", unbind = "removeProvider")
    private volatile List<LoadAverageProvider> providers = new ArrayList<>();
    private final Map<String, ScheduledFuture<?>> schedules = new HashMap<>();
    private long calcFreqMillis = 5000;
    private boolean running = false;

    public void addProvider(LoadAverageProvider provider) {
        LOGGER.info("Adding load average provider: {}", provider.getName());
        providers.add(provider);
        if (schedules.containsKey(provider.getClass().getName())) {
            schedules.get(provider.getClass().getName()).cancel(false);
            schedules.remove(provider.getClass().getName());
        }
        if (running) {
            ScheduledFuture<?> scheduledFuture =
                    executor.scheduleAtFixedRate(provider, 0, calcFreqMillis, TimeUnit.MILLISECONDS);
            schedules.put(provider.getClass().getName(), scheduledFuture);
        }
    }

    public void removeProvider(LoadAverageProvider provider) {
        LOGGER.info("Removing load average provider: {}", provider.getName());
        providers.remove(provider);
        if (schedules.containsKey(provider.getClass().getName())) {
            schedules.get(provider.getClass().getName()).cancel(false);
            schedules.remove(provider.getClass().getName());
        }
    }

    @Override
    public Optional<LoadAverageValue> findValue(String classname) {
        return providers.stream()
                .filter(p -> p.getClass().getName().equals(classname))
                .map(LoadAverageProvider::getAverage)
                .findFirst();
    }

    @Override
    public Optional<LoadAverageProvider> findProvider(String classname) {
        return providers.stream()
                .filter(provider -> provider.getClass().getName().equals(classname))
                .findFirst();
    }

    public String display() {
        return providers.stream().map(LoadAverageProvider::getInfo).collect(Collectors.joining("\r\n"));
    }

    /**
     * Sets the frequency, in milliseconds, at which the calculation of averages occurs.
     * @param millisec how many milliseconds between average calculations
     */
    public void setCalcFrequencyInMillisec(long millisec) {
        //TODO reschedule providers if already running
        //TODO maybe frequency should be delegated to each provider
        this.calcFreqMillis = millisec;
    }

    @Activate
    public void start() {
        LOGGER.info("Starting load average service...");
        if (calcFreqMillis > 0) {
            providers.forEach(provider -> {
                ScheduledFuture<?> scheduledFuture =
                        executor.scheduleAtFixedRate(provider, 0, calcFreqMillis, TimeUnit.MILLISECONDS);
                schedules.put(provider.getClass().getName(), scheduledFuture);
            });
            running = true;
        }
    }

    @Deactivate
    public void stop() {
        LOGGER.info("Stopping load average service");
        if (running) {
            running = false;
            try {
                executor.shutdown();
                if (!executor.awaitTermination(200, TimeUnit.MILLISECONDS)) { //optional *
                    LOGGER.info("Load Average Service did not terminate in the specified time."); //optional *
                    List<Runnable> droppedTasks = executor.shutdownNow(); //optional **
                    LOGGER.info("Load Average Service was abruptly shut down. " + droppedTasks.size() + " tasks will not be executed."); //optional **
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
            schedules.clear();
        }
    }
}
