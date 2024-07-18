import {healthCheck} from '../../support/gql';

describe('Module Spring Usage probe test', () => {
    it('Check that module spring usage probe exists and is green by default', () => {
        healthCheck({includes: 'ModulesSpringUsageProbe', severity: 'LOW'}).should(r => {
            expect(r.status.health).to.eq('GREEN');
            const moduleSpringUsageProbe = r.probes.find(probe => probe.name === 'ModulesSpringUsageProbe');
            expect(moduleSpringUsageProbe.status.health).to.eq('GREEN');
            expect(moduleSpringUsageProbe.severity).to.eq('MEDIUM');
        });
    });

    // Check the probe is green, deploy a non jahia module that does not use spring and check that the probe is green, undeploy module
    it('check that installing a module without spring usage keeps the probe green', {retries: 5}, function () {
        healthCheck({includes: 'ModulesSpringUsageProbe', severity: 'LOW'}).should(r => {
            expect(r.status.health).to.eq('GREEN');
        });

        cy.login();
        cy.installBundle('springUsageModuleProbe/no-spring-module-8.2.0.0.jar');
        cy.runProvisioningScript([{startBundle: 'no-spring-module/8.2.0.0'}]);
        healthCheck({includes: 'ModulesSpringUsageProbe', severity: 'LOW'}).should(r => {
            expect(r.status.health).to.eq('GREEN');
            expect(r.status.message).to.contains('(Jahia modules not checked)');
        });

        cy.runProvisioningScript([{uninstallBundle: 'no-spring-module/8.2.0.'}]);
        cy.logout();
    });

    // Deploy a module that uses spring but provided by jahia (groupId is: org.jahia.modules) and check that the probe is still green
    // Then change the probe config to include jahia provided modules and check that the probe becomes yellow
    it('check that jahia provided module even using spring does not affect probe', {retries: 5}, function () {
        healthCheck({includes: 'ModulesSpringUsageProbe', severity: 'LOW'}).should(r => {
            expect(r.status.health).to.eq('GREEN');
        });

        cy.login();
        cy.installBundle('springUsageModuleProbe/spring-jahia-gid-module-8.2.0.0.jar');
        cy.runProvisioningScript([{startBundle: 'spring-jahia-gid-module/8.2.0.0'}]);
        healthCheck({includes: 'ModulesSpringUsage', severity: 'LOW'}).should(r => {
            expect(r.status.health).to.eq('GREEN');
            expect(r.status.message).to.contains('(Jahia modules not checked)');
        });

        cy.runProvisioningScript({fileName: 'test-include-jahia-modules.json'});
        healthCheck({includes: 'ModulesSpringUsage', severity: 'LOW'}).should(r => {
            expect(r.status.health).to.eq('YELLOW');
            expect(r.status.message).to.contains('(Jahia modules checked)');
        });

        cy.runProvisioningScript({fileName: 'test-exclude-jahia-modules.json'});
        healthCheck({includes: 'ModulesSpringUsage', severity: 'LOW'}).should(r => {
            expect(r.status.health).to.eq('GREEN');
            expect(r.status.message).to.contains('(Jahia modules not checked)');
        });

        cy.runProvisioningScript([{uninstallBundle: 'spring-jahia-gid-module/8.2.0.0'}]);
        cy.logout();
    });

    // Check the probe is green, deploy a non jahia module that uses spring with beans.xml files (no import) and check that the probe is yellow, undeploy module
    it('check that module using spring beans declaration xml files are detected by probe', {retries: 5}, function () {
        healthCheck({includes: 'ModulesSpringUsage', severity: 'LOW'}).should(r => {
            expect(r.status.health).to.eq('GREEN');
        });

        cy.login();
        cy.installBundle('springUsageModuleProbe/spring-bean-module-8.2.0.0.jar');
        cy.runProvisioningScript([{startBundle: 'spring-bean-module/8.2.0.0'}]);
        healthCheck({includes: 'ModulesSpringUsage', severity: 'LOW'}).should(r => {
            expect(r.status.health).to.eq('YELLOW');
            expect(r.status.message).to.contains('package imported in OSGI manifest');
        });

        cy.runProvisioningScript([{uninstallBundle: 'spring-bean-module/8.2.0.0'}]);
        cy.logout();
    });

    // Check the probe is green, deploy a non jahia module that uses spring in imports and check that the probe is yellow, undeploy module
    it('check that module using spring osgi import package are detected by probe', {retries: 5}, function () {
        healthCheck({includes: 'ModulesSpringUsage', severity: 'LOW'}).should(r => {
            expect(r.status.health).to.eq('GREEN');
        });

        cy.login();
        cy.installBundle('springUsageModuleProbe/spring-import-module-8.2.0.0.jar');
        cy.runProvisioningScript([{startBundle: 'spring-import-module/8.2.0.0'}]);
        healthCheck({includes: 'ModulesSpringUsage', severity: 'LOW'}).should(r => {
            expect(r.status.health).to.eq('YELLOW');
            expect(r.status.message).to.contains('package imported in OSGI manifest');
        });

        cy.runProvisioningScript([{uninstallBundle: 'spring-import-module/8.2.0.0'}]);
        cy.logout();
    });
});
