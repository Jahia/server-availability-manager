import {healthCheck} from '../../support/gql';
import {healthCheckAPI} from '../../support/utils';

const PROBE = 'ModulesComponentState';

const ACTIVATION_MODULE = 'broken-activation-module/8.2.0.0';
const BIND_MODULE = 'broken-bind-module/8.2.0.0';

describe('Modules component state probe test', () => {
    const waitUntilOptions = {
        interval: 500,
        timeout: 15000,
        errorMsg: 'Failed to reach the expected probe health'
    };

    const waitUntilHealth = (health: string) => {
        cy.waitUntil(() =>
            healthCheck({includes: PROBE, severity: 'LOW'}).then(result => {
                // The probe can be missing for a moment while a bundle is refreshed, and that counts as a
                // failed poll rather than as an error.
                return result.probes.find(probe => probe.name === PROBE)?.status.health === health;
            }), waitUntilOptions);
    };

    // The hook asserts no starting state, so it also works when a previous run left the module installed.
    // Installing and starting a module that is already installed and started is accepted.
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

        // The blacklist tests edit a shared configuration, and a test that fails must not leave it applied to
        // the tests that follow.
        afterEach(() => {
            cy.runProvisioningScript({fileName: 'componentStateProbe/blacklist-clear.json'});
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

        it('is silenced by a blacklist on the module name', {retries: 5}, () => {
            cy.runProvisioningScript({fileName: 'componentStateProbe/blacklist-module.json'});
            waitUntilHealth('GREEN');
        });

        it('is silenced by a blacklist on the component name', {retries: 5}, () => {
            cy.runProvisioningScript({fileName: 'componentStateProbe/blacklist-component.json'});
            waitUntilHealth('GREEN');
        });

        // The health check response declares a Content-Length. A non-ASCII character in a probe message takes two
        // bytes and one character, so a response that counts characters is truncated and no longer parses.
        it('serves the whole response although the message carries a non-ASCII character', {retries: 5}, () => {
            healthCheckAPI({severity: 'LOW', includes: PROBE}).should(response => {
                // A truncated body does not parse, so response.body stays a string. Naming that first makes the
                // regression report its own cause instead of a type error.
                expect(response.status).to.eq(200);
                expect(response.body.probes).to.be.an('array').and.not.be.empty;
                const probe = response.body.probes.find(p => p.name === PROBE);
                expect(probe.status.message).to.contains('Deliberate activation failure (é)');
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
                expect(probe.status.message).to.contains('bind method could not be invoked');
            });
        });

        // This is the blind spot the probe exists to cover. The module is broken, and the module level probe
        // stays green, because Jahia marks a module STARTED from the bundle lifecycle alone.
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
