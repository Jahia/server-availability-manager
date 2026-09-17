import {healthCheck} from '../../support/gql';

const PROBE = 'ModulesComponentState';

const ACTIVATION_MODULE = 'broken-activation-module/8.2.0.0';
const BIND_MODULE = 'broken-bind-module/8.2.0.0';

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

    // A before hook can run again when Cypress retries, so it asserts no starting state. Installing and starting
    // a module that is already installed and started is accepted, and the probe is already YELLOW at that point.
    const installFixture = (module: string, jar: string) => {
        cy.installBundle(`componentStateProbe/${jar}`);
        cy.runProvisioningScript([{startBundle: module}]);
        waitUntilHealth('YELLOW');
    };

    const uninstallFixture = (module: string) => {
        cy.runProvisioningScript([{uninstallBundle: module}]);
        waitUntilHealth('GREEN');
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
            installFixture(ACTIVATION_MODULE, 'broken-activation-module-8.2.0.0.jar');
        });

        after(() => {
            uninstallFixture(ACTIVATION_MODULE);
        });

        it('reports the component and names the module', {retries: 5}, () => {
            healthCheck({includes: PROBE, severity: 'LOW'}).should(r => {
                const probe = r.probes.find(p => p.name === PROBE);
                expect(probe.status.health).to.eq('YELLOW');
                expect(probe.status.message).to.contains('broken-activation-module');
                expect(probe.status.message).to.contains('activation failed');
            });
        });

        // The health check response declares a Content-Length. A non-ASCII character in a probe message takes two
        // bytes and one character, so a response that counts characters is truncated and no longer parses.
        it('serves the whole response although the message carries a non-ASCII character', {retries: 5}, () => {
            cy.request({
                url: `${Cypress.config().baseUrl}/modules/healthcheck?severity=LOW&includes=${PROBE}`,
                headers: {referer: Cypress.config().baseUrl},
                auth: {user: 'root', pass: Cypress.env('SUPER_USER_PASSWORD'), sendImmediately: true},
                failOnStatusCode: false
            }).should(response => {
                expect(response.body.probes[0].status.message).to.contains('Deliberate activation failure (é)');
            });
        });
    });

    describe('Module whose bind method cannot be invoked', () => {
        before(() => {
            installFixture(BIND_MODULE, 'broken-bind-module-8.2.0.0.jar');
        });

        after(() => {
            uninstallFixture(BIND_MODULE);
        });

        it('reports the component although the module is started and the bundle is active', {retries: 5}, () => {
            healthCheck({includes: PROBE, severity: 'LOW'}).should(r => {
                const probe = r.probes.find(p => p.name === PROBE);
                expect(probe.status.health).to.eq('YELLOW');
                expect(probe.status.message).to.contains('broken-bind-module');
                expect(probe.status.message).to.contains('never activated');
            });
        });

        // The blind spot this probe exists to cover: the module is broken, and the module level probe stays green,
        // because Jahia marks a module STARTED from the bundle lifecycle alone.
        it('is not reported by the module level probe, which stays green', {retries: 5}, () => {
            healthCheck({includes: 'ModuleState', severity: 'LOW'}).should(r => {
                const moduleStateProbe = r.probes.find(p => p.name === 'ModuleState');
                expect(moduleStateProbe.status.health).to.eq('GREEN');
                expect(moduleStateProbe.status.message).to.not.contains('broken-bind-module');
            });
            cy.logout();
        });
    });
});
