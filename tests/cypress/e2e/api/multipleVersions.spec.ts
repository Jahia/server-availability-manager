import {healthCheck} from '../../support/gql';

describe('Multiple (Bundle|Module) probe test', () => {
    const isDevelopmentOperatingMode = Cypress.env('OPERATING_MODE') === 'development';

    it('Check that both probes are green', () => {
        healthCheck('LOW').should(r => {
            expect(r.status.health).to.eq('GREEN');

            let probe = r.probes.find(probe => probe.name === 'MultipleBundleVersions');
            expect(probe.status.health).to.eq('GREEN');
            expect(probe.severity).to.eq('CRITICAL');

            probe = r.probes.find(probe => probe.name === 'MultipleModuleVersions');
            expect(probe.status.health).to.eq('GREEN');
            expect(probe.severity).to.eq('MEDIUM');
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
                let probe = r.probes.find(probe => probe.name === 'MultipleBundleVersions');
                expect(probe.status.health).to.eq('GREEN');

                probe = r.probes.find(probe => probe.name === 'MultipleModuleVersions');
                expect(probe.status.health).to.eq(isDevelopmentOperatingMode ? 'GREEN' : 'YELLOW');
                expect(probe.status.message).to.contain('jahia-dashboard');
                expect(probe.status.message).to.contain('1.3.0: INSTALLED');
            });
        });
    });

    describe('Test multiple version of a bundle', () => {
        beforeEach(() => {
            cy.runProvisioningScript({fileName: 'multipleVersions/install-bundle.json'});
        });

        afterEach(() => {
            cy.runProvisioningScript({fileName: 'multipleVersions/uninstall-bundle.json'});
        });

        it('Checks the MultipleBundleVersions is reporting duplicate', () => {
            healthCheck('LOW').should(r => {
                let probe = r.probes.find(probe => probe.name === 'MultipleBundleVersions');
                expect(probe.status.health).to.eq(isDevelopmentOperatingMode ? 'YELLOW' : 'RED');
                expect(probe.status.message).to.contain('org.jahia.bundles.maintenancefilter');
                expect(probe.status.message).to.contain('8.2.0.4: INSTALLED');

                probe = r.probes.find(probe => probe.name === 'MultipleModuleVersions');
                expect(probe.status.health).to.eq('GREEN');
            });
        });
    });
});
