import {healthCheck} from '../../support/gql';

describe('Server Load probe test', () => {
    before('load graphql file and create test dataset', () => {
        cy.runProvisioningScript({fileName: 'serverLoadProbe/set-default-threshold.json'});
    });

    it('Check that server load probe is all green with default threshold parameters', () => {
        healthCheck('LOW').should(r => {
            expect(r.status.health).to.eq('GREEN');
            const serverLoadProbe = r.probes.find(probe => probe.name === 'ServerLoad');
            expect(serverLoadProbe.status.health).to.eq('GREEN');
            expect(serverLoadProbe.severity).to.eq('HIGH');
        });
    });

    it('Checks that server load probe is in YELLOW after changing the threshold to 0', () => {
        cy.runProvisioningScript({fileName: 'serverLoadProbe/set-yellow-threshold.json'});
        healthCheck('LOW').should(r => {
            expect(r.status.health).to.eq('YELLOW');
            const serverLoadProbe = r.probes.find(probe => probe.name === 'ServerLoad');
            expect(serverLoadProbe.status.health).to.eq('YELLOW');
        });
    });

    it('Checks that server load probe is in RED after changing the threshold to -1', () => {
        cy.runProvisioningScript({fileName: 'serverLoadProbe/set-red-threshold.json'});
        healthCheck('LOW').should(r => {
            expect(r.status.health).to.eq('RED');
            const serverLoadProbe = r.probes.find(probe => probe.name === 'ServerLoad');
            expect(serverLoadProbe.status.health).to.eq('RED');
        });
    });

    after('Set server load threshold back to the default', () => {
        cy.runProvisioningScript({fileName: 'serverLoadProbe/set-default-threshold.json'});
    });
});
