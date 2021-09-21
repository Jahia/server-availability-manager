package org.jahia.modules.sam.healthcheck.probes;

import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.utils.DatabaseUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Component(service = Probe.class, immediate = true)
public class DBConnectivityProbe implements Probe {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBConnectivityProbe.class);

    // The timeout value is defined in seconds.
    private int timeout = 20;

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
    public String getDescription() {
        return "Check DB connectivity";
    }

    @Override
    public String getName() {
        return "DBConnectivity";
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.CRITICAL;
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        if (config.containsKey("timeout")) {
            timeout = Integer.parseInt("timeout");
        }
    }
}
