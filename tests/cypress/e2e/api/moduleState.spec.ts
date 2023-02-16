import {healthCheck} from '../../support/gql';

describe('Module state probe test', () => {
    it('Check that module state probe is all green with no whitelists or blacklists', () => {
        healthCheck('LOW').should(r => {
            expect(r.status.health).to.eq('GREEN');
            const moduleStateProbe = r.probes.find(probe => probe.name === 'ModuleState');
            expect(moduleStateProbe.status.health).to.eq('GREEN');
            expect(moduleStateProbe.severity).to.eq('MEDIUM');
        });
    });

    describe('tests with 2 version of modules installed', () => {
        beforeEach(() => {
            cy.runProvisioningScript({fileName: 'moduleStateProbe/install-dashboard.json'});
        });

        afterEach(() => {
            cy.runProvisioningScript({fileName: 'moduleStateProbe/uninstall-dashboard.json'});
        });

        it('Checks the module state probe is YELLOW', () => {
            healthCheck('LOW').should(r => {
                expect(r.status.health).to.eq('YELLOW');
                expect(r.status.message).to.contain('jahia-dashboard');
                const moduleStateProbe = r.probes.find(probe => probe.name === 'ModuleState');
                expect(moduleStateProbe.status.health).to.eq('YELLOW');
                expect(moduleStateProbe.status.message).to.contain('jahia-dashboard');
            });
        });

        it('Checks the module state probe is GREEN when module is not in whitelist', () => {
            cy.runProvisioningScript({fileName: 'moduleStateProbe/enable-whitelist.json'});
            cy.runProvisioningScript({fileName: 'moduleStateProbe/stop-seo.json'});
            healthCheck('MEDIUM').should(r => {
                expect(r.status.health).to.eq('GREEN');
                const moduleStateProbe = r.probes.find(probe => probe.name === 'ModuleState');
                expect(moduleStateProbe.status.health).to.eq('GREEN');
            });
        });
    });

    it('Checks that module state probe is in RED after stopping the module', () => {
        cy.runProvisioningScript({fileName: 'moduleStateProbe/stop-channels.json'});
        healthCheck('LOW').should(r => {
            expect(r.status.health).to.eq('RED');
            expect(r.status.message).to.contain('channels');
            const moduleStateProbe = r.probes.find(probe => probe.name === 'ModuleState');
            expect(moduleStateProbe.status.health).to.eq('RED');
        });
    });

    it('Check that module state probe is green after we blacklist the module', () => {
        cy.runProvisioningScript({fileName: 'moduleStateProbe/enable-blacklist.json'});
        healthCheck('MEDIUM').should(r => {
            expect(r.status.health).to.eq('GREEN');
            const moduleStateProbe = r.probes.find(probe => probe.name === 'ModuleState');
            expect(moduleStateProbe.status.health).to.eq('GREEN');
        });
    });

    it('Checks that module state probe is GREEN even after we whitelisted the PAT module', () => {
        cy.runProvisioningScript({fileName: 'moduleStateProbe/enable-whitelist.json'});
        healthCheck('MEDIUM').should(r => {
            expect(r.status.health).to.eq('GREEN');
            const moduleStateProbe = r.probes.find(probe => probe.name === 'ModuleState');
            expect(moduleStateProbe.status.health).to.eq('GREEN');
        });
    });

    it('Checks the module state probe is GREEN when we stopped SEO module, which is not inside whitelist', () => {
        cy.runProvisioningScript({fileName: 'moduleStateProbe/enable-whitelist.json'});
        cy.runProvisioningScript({fileName: 'moduleStateProbe/stop-seo.json'});
        healthCheck('MEDIUM').should(r => {
            expect(r.status.health).to.eq('GREEN');
            const moduleStateProbe = r.probes.find(probe => probe.name === 'ModuleState');
            expect(moduleStateProbe.status.health).to.eq('GREEN');
        });
    });

    afterEach(() => {
        cy.runProvisioningScript({fileName: 'moduleStateProbe/start-seo.json'});
        cy.runProvisioningScript({fileName: 'moduleStateProbe/start-channels.json'});
        cy.runProvisioningScript({fileName: 'moduleStateProbe/disable-blacklist.json'});
        cy.runProvisioningScript({fileName: 'moduleStateProbe/disable-whitelist.json'});
    });
});
