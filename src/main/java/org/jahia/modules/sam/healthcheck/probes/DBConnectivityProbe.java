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

@Component(service = Probe.class, immediate = true)
public class DBConnectivityProbe implements Probe {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBConnectivityProbe.class);

    @Override
    public ProbeStatus getStatus() {
        try (Connection conn = DatabaseUtils.getDatasource().getConnection()) {
            // The timeout value is defined in seconds.
            if (conn.isValid(20)) {
                return ProbeStatus.GREEN;
            } else {
                return ProbeStatus.RED;
            }
        } catch (SQLException ex) {
            LOGGER.debug("Impossible to check the validity of the DB connection", ex);
            return ProbeStatus.RED;
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
}