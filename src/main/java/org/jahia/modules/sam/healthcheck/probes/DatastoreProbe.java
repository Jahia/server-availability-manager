package org.jahia.modules.sam.healthcheck.probes;

import org.apache.jackrabbit.core.JahiaSessionImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.services.content.JCRTemplate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import org.apache.jackrabbit.core.data.DataStore;
import org.apache.jackrabbit.core.data.db.DbDataStore;

@Component(service = Probe.class, immediate = true)
public class DatastoreProbe implements Probe {

    private static final Logger logger = LoggerFactory.getLogger(DatastoreProbe.class);

    @Override
    public ProbeStatus getStatus() {

        if (!isStoreFilesInDB()) {
            final String datastoreHome = System.getProperty("jahia.jackrabbit.datastore.path");
            Path datastorePath = Paths.get(datastoreHome);

            return Files.exists(datastorePath) && Files.isWritable(datastorePath) ?
                    new ProbeStatus("Datastore is healthy", ProbeStatus.Health.GREEN) :
                    new ProbeStatus("Could not perform write operation", ProbeStatus.Health.RED);
        }

        return new ProbeStatus("Datastore is healthy", ProbeStatus.Health.GREEN);

    }

    private boolean isStoreFilesInDB() {
        try {
            return JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, null, null, session -> {
                final SessionImpl providerSession = (SessionImpl) session.getProviderSession(session.getNode("/").getProvider());
                DataStore dataStore = ((JahiaSessionImpl) providerSession).getContext().getDataStore();
                return dataStore instanceof DbDataStore;
            });
        } catch (RepositoryException e) {
            logger.error("Error trying to get dataStore information", e);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Checks the connectivity with the JCR Datastore";
    }

    @Override
    public String getName() {
        return "Datastore";
    }

    @Override
    public ProbeSeverity getDefaultSeverity() {
        return ProbeSeverity.CRITICAL;
    }
}
