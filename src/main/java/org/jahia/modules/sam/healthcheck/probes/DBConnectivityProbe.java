package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.utils.DatabaseUtils;
import org.osgi.service.component.annotations.Component;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Component(service = Probe.class, immediate = true)
public class DBConnectivityProbe extends AbstractProbe {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBConnectivityProbe.class);

    // The timeout value is defined in seconds.
    private static final String TIMEOUT_CONFIG_PROPERTY = "timeout";

    private volatile int timeout = 20;

    public DBConnectivityProbe() {
        super("DBConnectivity", "Check DB connectivity", ProbeSeverity.CRITICAL);
    }

    @Override
    public ProbeStatus getStatus() {
        try (Connection conn = DatabaseUtils.getDatasource().getConnection()) {
            if (conn.isValid(timeout)) {
                return new ProbeStatus("Connection established", ProbeStatus.Health.GREEN);
            } else {
                return new ProbeStatus("Could not connect", ProbeStatus.Health.RED);
            }
        } catch (SQLException ex) {
            LOGGER.debug("Impossible to check the validity of the DB connection", ex);
            return new ProbeStatus("Encountered exception while connecting", ProbeStatus.Health.RED);
        }

    }

    @Override
    public void setConfig(Map<String, Object> config) {
        // This read the key name rather than its value, so it threw on every update that carried the property.
        String configured = String.valueOf(config.get(TIMEOUT_CONFIG_PROPERTY));
        if (config.containsKey(TIMEOUT_CONFIG_PROPERTY) && StringUtils.isNotEmpty(configured)) {
            try {
                timeout = Integer.parseInt(configured);
            } catch (NumberFormatException e) {
                LOGGER.warn("The {} property of this probe is not a number, so the probe keeps {} seconds: {}",
                        TIMEOUT_CONFIG_PROPERTY, timeout, configured);
            }
        }
    }
}
