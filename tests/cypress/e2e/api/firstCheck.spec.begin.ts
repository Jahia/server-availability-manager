import {waitUntilSAMStatusGreen} from '@jahia/cypress';
import {healthCheck} from '../../support/gql';

// This suite is there to ensure we get wait for SAM to return GREEN before starting the tests
// The goal is to wait for any provisioning activities that might be happening, in particular in cluster
// Failure of this suite highlights a potential issue with Jahia itself

describe('Absence of errors in SAM at startup', () => {
    it('Wait until SAM returns GREEN for medium severity', () => {
        // The timeout of 3mn (180) is there to allow for the cluster (if present) to finish its synchronization
        waitUntilSAMStatusGreen('MEDIUM', 180000);
        cy.executeGroovy('groovy/logProvisioningDone.groovy');
    });

    it('Check that SAM overall status is green for MEDIUM severity', () => {
        healthCheck({severity: 'DEBUG'}).then(r => {
            cy.log('Probes not GREEN with severity DEBUG: ' + JSON.stringify(r.probes.filter(probe => probe.status.health !== 'GREEN')));
        });

        healthCheck({severity: 'MEDIUM'}).then(r => {
            cy.then(() => {
                expect(r.status.health).to.eq('GREEN');
            });
        });
    });
});
