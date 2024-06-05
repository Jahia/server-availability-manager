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

import org.jahia.tools.jvm.ThreadMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jahia.modules.sam.load.LoadAverageValue.*;

/**
 * Abstract class that defines a specific average in the LoadAverageMonitor
 * This class makes it easy to calculate a load average, using an average calculation like the following formula:
 * load(t) = load(t – 1) e^(-5/60m) + n (1 – e^(-5/60m))
 * where n = what we are evaluating over time (number of active threads, requests, etc...)
 * and m = time in minutes over which to perform the average
 *
 */

public abstract class LoadAverageProvider implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadAverageProvider.class);

    private final LoadAverageValue average;
    private boolean threadDumpOnHighLoad;
    private double loggingTriggerValue;
    private double threadDumpTriggerValue;

    public LoadAverageProvider(String name) {
        average = new LoadAverageValue(name);
        loggingTriggerValue = 0.0;
        threadDumpTriggerValue = 0.0;
    }

    public abstract String getName();

    public abstract double getValue();

    public boolean isThreadDumpOnHighLoad() {
        return threadDumpOnHighLoad;
    }

    public void setThreadDumpOnHighLoad(boolean threadDumpOnHighLoad) {
        this.threadDumpOnHighLoad = threadDumpOnHighLoad;
    }

    public double getLoggingTriggerValue() {
        return loggingTriggerValue;
    }

    public void setLoggingTriggerValue(double loggingTriggerValue) {
        this.loggingTriggerValue = loggingTriggerValue;
    }

    public double getThreadDumpTriggerValue() {
        return threadDumpTriggerValue;
    }

    public void setThreadDumpTriggerValue(double threadDumpTriggerValue) {
        this.threadDumpTriggerValue = threadDumpTriggerValue;
    }

    public LoadAverageValue getAverage() {
        return average;
    }

    public void run() {
        if (getAverage().getLastRun() > 0) {
            double calcFreqDouble = (System.currentTimeMillis() - getAverage().getLastRun()) / 1000d;
            average.setOneMinuteLoad(average.getOneMinuteLoad() *
                    Math.exp(-calcFreqDouble / (60.0 * ONE_MINUTE)) + getValue() * (1 - Math.exp(-calcFreqDouble / (60.0 * ONE_MINUTE))));
            average.setFiveMinuteLoad(average.getFiveMinuteLoad() *
                    Math.exp(-calcFreqDouble / (60.0 * FIVE_MINUTES)) + getValue() * (1 - Math.exp(-calcFreqDouble / (60.0 * FIVE_MINUTES))));
            average.setFifteenMinuteLoad(average.getFifteenMinuteLoad() *
                    Math.exp(-calcFreqDouble / (60.0 * FIFTEEN_MINUTES)) + getValue() * (1 - Math.exp(-calcFreqDouble / (60.0 * FIFTEEN_MINUTES))));
            tickCallback();
        }
        getAverage().setLastRun(System.currentTimeMillis());
    }

    public void tickCallback() {
        if (getLoggingTriggerValue() > 0 && average.getOneMinuteLoad() > getLoggingTriggerValue()) {
            LOGGER.info(average.toString());
            if (isThreadDumpOnHighLoad() && average.getOneMinuteLoad() > getThreadDumpTriggerValue()) {
                ThreadMonitor.getInstance().dumpThreadInfo(false, true);
            }
        }
    }

    public String getInfo() {
        return average.toString();
    }
}
