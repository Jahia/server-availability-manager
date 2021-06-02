package org.jahia.modules.sam;

import java.util.Map;

public interface Probe {

    /**
     * Unique name of this probe
     * @return name
     */
    String getName();

    /**
     * Short description of this probe
     * @return name
     */
    String getDescription();

    /**
     * Current status
     * @return status
     */
    ProbeStatus getStatus();


    /**
     * Probe severity
     * @return severity
     */
    default ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.LOW;
    }

    /**
     * Specific configuration for this probe, from org.jahia.modules.sam.core.ProbesRegistry.cfg file
     * @param config config
     */
    default void setConfig(Map<String, Object> config) {
        //
    }

}
