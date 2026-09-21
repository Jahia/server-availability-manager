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
                // A bundle refresh can take the GraphQL provider down for a moment. The whole result is then
                // undefined, and the probe can also be missing from a result that arrived. Both count as a
                // failed poll, because a throw here would fail the hook instead of retrying.
                return result?.probes?.find(probe => probe.name === PROBE)?.status?.health === health;
            }), waitUntilOptions);
    };

    // The hook makes no assumption about the starting state, so it also works when a previous run left the
    // module installed. Installing and starting a module that is already installed and started is accepted.
    const installFixture = (module: string, jar: string) => {
        cy.installBundle(`componentStateProbe/${jar}`);
        cy.runProvisioningScript([{startBundle: module}]);
        waitUntilHealth('YELLOW');
    };

    const uninstallFixture = (module: string) => {
        cy.runProvisioningScript([{uninstallBundle: module}]);
        waitUntilHealth('GREEN');
    };

    const FIXTURE_NAMES = [ACTIVATION_MODULE, BIND_MODULE].map(module => module.split('/')[0]);

    /**
     * Reads `bundle:list -s`, whose columns are the id, the state, the start level, the version and the
     * symbolic name. A fixture is matched on the symbolic name column rather than on the raw text, and it is
     * returned at the version the instance carries, which is not always the version this spec installs.
     *
     * Karaf separates the columns with an ASCII pipe here, and it draws a box-drawing pipe on a terminal that
     * takes one, so the split accepts both.
     */
    const findLeftoverFixtures = (bundles: string): string[] => bundles
        // eslint-disable-next-line no-control-regex
        .replace(/\u001b\[[0-9;]*m/g, '')
        .split('\n')
        .map(line => line.split(/[|\u2502]/).map(column => column.trim()))
        .filter(columns => columns.length >= 5 && FIXTURE_NAMES.includes(columns[4]))
        .map(columns => `${columns[4]}/${columns[3]}`);

    // The provisioning API answers 500 for a bundle it cannot find.
    // A fixture is therefore uninstalled only once the instance is known to carry it. The bundle list is read
    // over ssh, which reports the real state whatever the probe configuration says.
    const removeLeftoverFixtures = () => {
        cy.task('sshCommand', ['bundle:list -s']).then((bundles: string) => {
            const leftovers = findLeftoverFixtures(bundles);
            if (leftovers.length > 0) {
                cy.runProvisioningScript(leftovers.map(module => ({uninstallBundle: module})));
                waitUntilHealth('GREEN');
            }
        });
    };

    // A run interrupted before the after() hooks leaves two kinds of residue.
    // An installed fixture module is the worse one. The probe scans every started module, so the next run
    // fails on every global GREEN assertion of the suite, before this spec is even reached.
    // A configured blacklist is the other one. Removing the fixtures already makes it harmless here, because
    // the describe hooks below install a fixture and wait for YELLOW, which a stale blacklist would prevent.
    before(() => {
        removeLeftoverFixtures();
        cy.runProvisioningScript({fileName: 'componentStateProbe/blacklist-clear.json'});
        waitUntilHealth('GREEN');
    });

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

        // The blacklist tests edit a shared configuration, and a failing test must not leave it applied to the
        // tests that follow.
        // The clear reaches the probe through ConfigurationAdmin and the @Modified callback, so the hook waits
        // for the probe to report the fixture again. Without that wait, a following test reads the residue of
        // this one and passes on it.
        afterEach(() => {
            cy.runProvisioningScript({fileName: 'componentStateProbe/blacklist-clear.json'});
            waitUntilHealth('YELLOW');
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
        });
    });
});
