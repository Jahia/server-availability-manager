package org.jahia.modules.sam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        return ProbeSeverity.IGNORED;
    }

    /**
     * Specific configuration for this probe, from org.jahia.modules.sam.core.ProbesRegistry.cfg file
     * @param config config
     */
    default void setConfig(Map<String, Object> config) {
        //
    }

    default boolean needsHttpContext() {
        return false;
    }

    default ProbeStatus getStatus(HttpServletRequest request, HttpServletResponse response) {
        return getStatus(); // default implementation does not use request/response
    }
}
