import {healthCheck} from '../../support/gql';

// Skipped as part of https://github.com/Jahia/jahia-private/issues/4280 (use of OpenJDK by default instead of GraalVM).
describe.skip('Supported Stack JS Modules', () => {
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
