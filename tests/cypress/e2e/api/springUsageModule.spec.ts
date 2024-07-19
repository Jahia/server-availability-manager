import {healthCheck} from '../../support/gql';

describe('Module Spring Usage probe test', () => {
    const waitUntilOptions = {
        interval: 500,
        timeout: 10000,
        errorMsg: 'Failed to verify configuration update'
    };

    const waitUntilHealth = (health: string, probeName: string) => {
        cy.waitUntil(() =>
            healthCheck({includes: probeName, severity: 'LOW'}).then(result => {
                return result.probes.find(probe => probe.name === probeName).status.health === health;
            }), waitUntilOptions);
    };

    it('Check that module spring usage probe exists and is green by default', () => {
        healthCheck({includes: 'ModulesSpringUsage', severity: 'LOW'}).should(r => {
            expect(r.status.health).to.eq('GREEN');
            const moduleSpringUsageProbe = r.probes.find(probe => probe.name === 'ModulesSpringUsage');
            expect(moduleSpringUsageProbe.status.health).to.eq('GREEN');
            expect(moduleSpringUsageProbe.severity).to.eq('MEDIUM');
        });
    });

    describe('Module without spring test: probe should stay green after installation', () => {
        before(() => {
            cy.installBundle('springUsageModuleProbe/no-spring-module-8.2.0.0.jar');
            cy.runProvisioningScript([{startBundle: 'no-spring-module/8.2.0.0'}]);
            waitUntilHealth('GREEN', 'ModulesSpringUsage');
        });

        after(() => {
            cy.runProvisioningScript([{uninstallBundle: 'no-spring-module/8.2.0.0'}]);
            waitUntilHealth('GREEN', 'ModulesSpringUsage');
        });

        it('No spring in the module, the probe is green', () => {
            healthCheck({includes: 'ModulesSpringUsage', severity: 'LOW'}).should(r => {
                expect(r.status.health).to.eq('GREEN');
                const moduleSpringUsageProbe = r.probes.find(probe => probe.name === 'ModulesSpringUsage');
                expect(moduleSpringUsageProbe.status.health).to.eq('GREEN');
            });
        });
    });

    describe('Module with spring but provided by jahia', () => {
        before(() => {
            waitUntilHealth('GREEN', 'ModulesSpringUsage');
            cy.installBundle('springUsageModuleProbe/spring-jahia-gid-module-8.2.0.0.jar');
            cy.runProvisioningScript([{startBundle: 'spring-jahia-gid-module/8.2.0.0'}]);
            waitUntilHealth('GREEN', 'ModulesSpringUsage');
        });

        after(() => {
            cy.runProvisioningScript([{uninstallBundle: 'spring-jahia-gid-module/8.2.0.0'}]);
            cy.runProvisioningScript({fileName: 'test-exclude-jahia-modules.json'});
            waitUntilHealth('GREEN', 'ModulesSpringUsage');
        });

        it('Basic configuration ignores jahia provided modules', () => {
            healthCheck({includes: 'ModulesSpringUsage', severity: 'LOW'}).should(r => {
                expect(r.status.health).to.eq('GREEN');
                const moduleSpringUsageProbe = r.probes.find(probe => probe.name === 'ModulesSpringUsage');
                expect(moduleSpringUsageProbe.status.health).to.eq('GREEN');
                expect(moduleSpringUsageProbe.status.message).to.contains('(Jahia modules not checked)');
            });
        });

        it('Set configuration to include jahia provided packages', () => {
            cy.runProvisioningScript({fileName: 'test-include-jahia-modules.json'});
            waitUntilHealth('YELLOW', 'ModulesSpringUsage');
            healthCheck({includes: 'ModulesSpringUsage', severity: 'LOW'}).should(r => {
                expect(r.status.health).to.eq('YELLOW');
                const moduleSpringUsageProbe = r.probes.find(probe => probe.name === 'ModulesSpringUsage');
                expect(moduleSpringUsageProbe.status.health).to.eq('YELLOW');
                expect(moduleSpringUsageProbe.status.message).to.contains('(Jahia modules checked)');
            });
        });

        it('Set configuration to exclude jahia provided packages', () => {
            cy.runProvisioningScript({fileName: 'test-exclude-jahia-modules.json'});
            waitUntilHealth('GREEN', 'ModulesSpringUsage');
            healthCheck({includes: 'ModulesSpringUsage', severity: 'LOW'}).should(r => {
                expect(r.status.health).to.eq('GREEN');
                const moduleSpringUsageProbe = r.probes.find(probe => probe.name === 'ModulesSpringUsage');
                expect(moduleSpringUsageProbe.status.health).to.eq('GREEN');
                expect(moduleSpringUsageProbe.status.message).to.contains('(Jahia modules not checked)');
            });
        });
    });

    describe('Module with spring in a xml declaration', () => {
        before(() => {
            waitUntilHealth('GREEN', 'ModulesSpringUsage');
            cy.installBundle('springUsageModuleProbe/spring-bean-module-8.2.0.0.jar');
            cy.runProvisioningScript([{startBundle: 'spring-bean-module/8.2.0.0'}]);
            waitUntilHealth('YELLOW', 'ModulesSpringUsage');
        });

        after(() => {
            cy.runProvisioningScript([{uninstallBundle: 'spring-bean-module/8.2.0.0'}]);
            waitUntilHealth('GREEN', 'ModulesSpringUsage');
        });

        it('check that module using spring beans declaration xml files are detected by probe', {retries: 5}, function () {
            healthCheck({includes: 'ModulesSpringUsage', severity: 'LOW'}).should(r => {
                expect(r.status.health).to.eq('YELLOW');
                expect(r.status.message).to.contains('detected a Spring context xml file');
            });
        });
    });

    describe('Module with spring in the imported packages', () => {
        before(() => {
            waitUntilHealth('GREEN', 'ModulesSpringUsage');
            cy.installBundle('springUsageModuleProbe/spring-import-module-8.2.0.0.jar');
            cy.runProvisioningScript([{startBundle: 'spring-import-module/8.2.0.0'}]);
            waitUntilHealth('YELLOW', 'ModulesSpringUsage');
        });

        after(() => {
            cy.runProvisioningScript([{uninstallBundle: 'spring-import-module/8.2.0.0'}]);
            waitUntilHealth('GREEN', 'ModulesSpringUsage');
        });

        it('check that module using spring osgi import package are detected by probe', {retries: 5}, function () {
            healthCheck({includes: 'ModulesSpringUsage', severity: 'LOW'}).should(r => {
                expect(r.status.health).to.eq('YELLOW');
                expect(r.status.message).to.contains('package imported in OSGI manifest');
            });
            cy.logout();
        });
    });
});
