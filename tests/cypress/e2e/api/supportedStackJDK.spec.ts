import {healthCheck} from '../../support/gql';

describe('Supported Stack JDK', () => {
    it('Verifies JDK probe is present', () => {
        cy.login();
        healthCheck('MEDIUM').should(r => {
            expect(r.status.health).to.eq('GREEN');
            const supportedStackJDKProbe = r.probes.find(probe => probe.name === 'SupportedStackJDK');
            expect(supportedStackJDKProbe.status.health).to.eq('GREEN');
        });
        cy.logout();
    });
});