import {healthCheck} from '../../support/gql';

const PROBE = 'ModulesComponentState';

describe('Modules component state probe test', () => {
    const waitUntilOptions = {
        interval: 500,
        // The probe serves a cached result until SCR republishes its change count, which it does 5 s after the
        // last component state change. Every health transition therefore needs more than 5 s to appear.
        timeout: 30000,
        errorMsg: 'Failed to reach the expected probe health'
    };

    const waitUntilHealth = (health: string) => {
        cy.waitUntil(() =>
            healthCheck({includes: PROBE, severity: 'LOW'}).then(result => {
                return result.probes.find(probe => probe.name === PROBE).status.health === health;
            }), waitUntilOptions);
    };

    it('Check that the probe exists and is green by default', () => {
        healthCheck({includes: PROBE, severity: 'LOW'}).should(r => {
            expect(r.status.health).to.eq('GREEN');
            const probe = r.probes.find(p => p.name === PROBE);
            expect(probe.status.health).to.eq('GREEN');
            expect(probe.severity).to.eq('MEDIUM');
        });
    });

    describe('Module whose component throws on activation', () => {
        before(() => {
            waitUntilHealth('GREEN');
            cy.installBundle('componentStateProbe/broken-activation-module-8.2.0.0.jar');
            cy.runProvisioningScript([{startBundle: 'broken-activation-module/8.2.0.0'}]);
            waitUntilHealth('YELLOW');
        });

        after(() => {
            cy.runProvisioningScript([{uninstallBundle: 'broken-activation-module/8.2.0.0'}]);
            waitUntilHealth('GREEN');
        });

        it('reports the component and names the module', {retries: 5}, () => {
            healthCheck({includes: PROBE, severity: 'LOW'}).should(r => {
                const probe = r.probes.find(p => p.name === PROBE);
                expect(probe.status.health).to.eq('YELLOW');
                expect(probe.status.message).to.contains('broken-activation-module');
                expect(probe.status.message).to.contains('activation failed');
            });
        });
    });

    describe('Module whose bind method cannot be invoked', () => {
        before(() => {
            waitUntilHealth('GREEN');
            cy.installBundle('componentStateProbe/broken-bind-module-8.2.0.0.jar');
            cy.runProvisioningScript([{startBundle: 'broken-bind-module/8.2.0.0'}]);
            waitUntilHealth('YELLOW');
        });

        after(() => {
            cy.runProvisioningScript([{uninstallBundle: 'broken-bind-module/8.2.0.0'}]);
            waitUntilHealth('GREEN');
        });

        it('reports the component although the module is started and the bundle is active', {retries: 5}, () => {
            healthCheck({includes: PROBE, severity: 'LOW'}).should(r => {
                const probe = r.probes.find(p => p.name === PROBE);
                expect(probe.status.health).to.eq('YELLOW');
                expect(probe.status.message).to.contains('broken-bind-module');
                expect(probe.status.message).to.contains('never activated');
            });
        });

        // The blind spot this probe exists to cover: the module is broken, and the module-level probe says nothing
        // about it, because Jahia marks a module STARTED from the bundle lifecycle alone.
        it('is not reported at all by the module level probe', {retries: 5}, () => {
            healthCheck({includes: 'ModuleState', severity: 'LOW'}).should(r => {
                const moduleStateProbe = r.probes.find(p => p.name === 'ModuleState');
                expect(moduleStateProbe.status.message).to.not.contains('broken-bind-module');
            });
            cy.logout();
        });
    });
});
