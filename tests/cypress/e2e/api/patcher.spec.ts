import {healthCheck} from '../../support/gql';

describe('Server Load probe test', () => {
    before('load graphql file and create test dataset', () => {
        cy.runProvisioningScript({fileName: 'patcherProbe/runSkipPatchScript.json'});
    });

    it('Check that patcher  probe is all green after startup', () => {
        healthCheck('CRITICAL').should(r => {
            expect(r.status.health).to.eq('GREEN');
            const serverLoadProbe = r.probes.find(probe => probe.name === 'PatchFailures');
            expect(serverLoadProbe.status.health).to.eq('GREEN');
            expect(serverLoadProbe.severity).to.eq('CRITICAL');
        });
    });

    it('Check that patcher  probe is red after failing a patch', () => {
        cy.runProvisioningScript({fileName: 'patcherProbe/runFailPatchScript.json'});
        healthCheck('CRITICAL').should(r => {
            expect(r.status.health).to.eq('RED');
            const serverLoadProbe = r.probes.find(probe => probe.name === 'PatchFailures');
            expect(serverLoadProbe.status.health).to.eq('RED');
            expect(serverLoadProbe.severity).to.eq('CRITICAL');
        });
    });

    after('Restoring state', () => {
        cy.runProvisioningScript({fileName: 'patcherProbe/runSkipPatchScript.json'});
    });
});
