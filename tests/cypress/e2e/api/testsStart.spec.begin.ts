import {waitUntilSAMStatusGreen} from '@jahia/cypress';
import {healthCheck} from '../../support/gql';

describe('Absence of errors in SAM at startup', () => {
    it('Wait until SAM returns GREEN for medium severity', () => {
        // The timeout of 3mn (180) is there to allow for the cluster to finish its synchronization
        waitUntilSAMStatusGreen('MEDIUM', 180000);
        cy.executeGroovy('groovy/logProvisioningDone.groovy');
    });

    it('Check that SAM overall status is green for MEDIUM severity', () => {
        healthCheck({severity: 'DEBUG'}).then(r => {
            cy.log('Probes not GREEN with severity DEBUG: ' + JSON.stringify(r.probes.filter(probe => probe.status.health !== 'GREEN')));
        });

        healthCheck({severity: 'MEDIUM'}).then(r => {
            cy.then(() => {
                expect(r.status.health).to.eq('DEBUG');
            });
        });
    });
});