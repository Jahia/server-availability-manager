import {healthCheck} from '../../support/gql';

describe('Multiple (Bundle|Module) probe test', () => {
    const isDevelopmentOperatingMode = Cypress.env('OPERATING_MODE') === 'development';

    it('Check that both probes are green', () => {
        healthCheck('LOW').should(r => {
            expect(r.status.health).to.eq('GREEN');

            let probeToCheck = r.probes.find(probe => probe.name === 'MultipleBundleVersions');
            expect(probeToCheck.status.health).to.eq('GREEN');
            expect(probeToCheck.severity).to.eq('HIGH');

            probeToCheck = r.probes.find(probe => probe.name === 'MultipleModuleVersions');
            expect(probeToCheck.status.health).to.eq('GREEN');
            expect(probeToCheck.severity).to.eq('MEDIUM');
        });
    });

    describe('Test multiple version of a module', () => {
        beforeEach(() => {
            cy.runProvisioningScript({fileName: 'multipleVersions/install-module.json'});
        });

        afterEach(() => {
            cy.runProvisioningScript({fileName: 'multipleVersions/uninstall-module.json'});
        });

        it('Checks the MultipleModuleVersions is reporting duplicate', () => {
            healthCheck('LOW').should(r => {
                let probeToCheck = r.probes.find(probe => probe.name === 'MultipleBundleVersions');
                expect(probeToCheck.status.health).to.eq('GREEN');

                probeToCheck = r.probes.find(probe => probe.name === 'MultipleModuleVersions');
                expect(probeToCheck.status.health).to.eq(isDevelopmentOperatingMode ? 'GREEN' : 'YELLOW');
                expect(probeToCheck.status.message).to.contain('jahia-dashboard');
                expect(probeToCheck.status.message).to.contain('1.3.0: INSTALLED');
            });
        });
    });
});
