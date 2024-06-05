/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.modules.sam.load;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author Jerome Blanchard
 */
public class LoadAverageValue {

    public static final LoadAverageValue EMPTY = new LoadAverageValue("Entry does NOT exists");
    public static final Double ONE_MINUTE = 1.0;
    public static final Double FIVE_MINUTES = 5.0;
    public static final Double FIFTEEN_MINUTES = 15.0;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    private final String name;
    private double oneMinuteLoad = 0.0;
    private double fiveMinuteLoad = 0.0;
    private double fifteenMinuteLoad = 0.0;
    private long lastRun = 0;

    public LoadAverageValue(String name) {
        this.name = name;
    }

    public double getOneMinuteLoad() {
        return oneMinuteLoad;
    }

    public void setOneMinuteLoad(double oneMinuteLoad) {
        this.oneMinuteLoad = oneMinuteLoad;
    }

    public double getFiveMinuteLoad() {
        return fiveMinuteLoad;
    }

    public void setFiveMinuteLoad(double fiveMinuteLoad) {
        this.fiveMinuteLoad = fiveMinuteLoad;
    }

    public double getFifteenMinuteLoad() {
        return fifteenMinuteLoad;
    }

    public void setFifteenMinuteLoad(double fifteenMinuteLoad) {
        this.fifteenMinuteLoad = fifteenMinuteLoad;
    }

    public long getLastRun() {
        return lastRun;
    }

    public void setLastRun(long lastRun) {
        this.lastRun = lastRun;
    }

    @Override
    public String toString() {
        return name.concat(" = ").concat(String.valueOf(oneMinuteLoad))
                .concat(" ").concat(String.valueOf(fiveMinuteLoad))
                .concat(" ").concat(String.valueOf(fifteenMinuteLoad))
                .concat(" [").concat(FORMATTER.format(Instant.ofEpochMilli(lastRun))).concat("]");
    }

}
