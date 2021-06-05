package org.jahia.modules.sam.healthcheck.probes;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.persistence.PersistenceManager;
import org.apache.jackrabbit.core.persistence.pool.BundleDbPersistenceManager;
import org.apache.jackrabbit.core.version.InternalVersionManager;
import org.apache.jackrabbit.core.version.InternalVersionManagerImpl;
import org.apache.jackrabbit.core.version.InternalXAVersionManager;
import org.jahia.modules.sam.Probe;
import org.jahia.modules.sam.ProbeSeverity;
import org.jahia.modules.sam.ProbeStatus;
import org.jahia.services.content.JCRTemplate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.File;

@Component(service = Probe.class, immediate = true)
public class DatastoreProbe implements Probe {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatastoreProbe.class);

    @Override
    public ProbeStatus getStatus() {
        if (isDbPersistenceManager()) {
            return ProbeStatus.GREEN;
        }
        final String datastoreHome = System.getProperty("jahia.jackrabbit.datastore.path");
        return (new File(datastoreHome)).canWrite() ? ProbeStatus.GREEN : ProbeStatus.RED;
    }

    private boolean isDbPersistenceManager() {
        try {
            return JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, null, null, session -> {
                final SessionImpl providerSession = (SessionImpl) session.getProviderSession(session.getNode("/").getProvider());
                final InternalVersionManager vm = providerSession.getInternalVersionManager();
                PersistenceManager pm;
                if (vm instanceof InternalVersionManagerImpl) {
                    pm = ((InternalVersionManagerImpl) vm).getPersistenceManager();
                } else if (vm instanceof InternalXAVersionManager) {
                    pm = ((InternalXAVersionManager) vm).getPersistenceManager();
                } else {
                    LOGGER.warn("Unknown implementation of the InternalVersionManager: {}.", vm.getClass().getName());
                    return false;
                }
                return pm instanceof BundleDbPersistenceManager;
            });
        } catch (RepositoryException e) {
            LOGGER.error("", e);
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
