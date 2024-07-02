import {healthCheck} from '../../support/gql';

describe('Supported Stack JS Modules', () => {
    it('Verifies JS Modules probe is present', () => {
        cy.login();
        healthCheck('MEDIUM').should(r => {
            expect(r.status.health).to.eq('GREEN');
            const supportedStackJDKProbe = r.probes.find(probe => probe.name === 'SupportedStackJSModules');
            expect(supportedStackJDKProbe.status.health).to.eq('GREEN');
        });
        cy.logout();
    });
});
