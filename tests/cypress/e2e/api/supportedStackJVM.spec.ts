import {healthCheck} from '../../support/gql';

describe('Supported Stack JVM', () => {
    it('Verifies JVM probe is present', () => {
        cy.login();
        healthCheck('MEDIUM').should(r => {
            expect(r.status.health).to.eq('GREEN');
            const supportedStackJVMProbe = r.probes.find(probe => probe.name === 'SupportedStackJVM');
            expect(supportedStackJVMProbe.status.health).to.eq('GREEN');
        });
        cy.logout();
    });
});
