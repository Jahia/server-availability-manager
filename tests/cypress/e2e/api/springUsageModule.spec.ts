import {healthCheck} from '../../support/gql';

describe('Module Spring Usage probe test', () => {
    it('Check that module spring usage probe exists and is green by default', () => {
        healthCheck({includes:'ModuleSpringUsage', severity:'LOW'}).should(r => {
            expect(r.status.health).to.eq('GREEN');
            const moduleSpringUsageProbe = r.probes.find(probe => probe.name === 'ModuleSpringUsageProbe');
            expect(moduleSpringUsageProbe.status.health).to.eq('GREEN');
            expect(moduleSpringUsageProbe.severity).to.eq('MEDIUM');
        });
    });

    //Check the probe is green, deploy a non jahia module that does not use spring and check that the probe is green, undeploy module
    it('check that installing a module without spring usage keeps the probe green', {retries: 5}, function () {
        cy.login();
        healthCheck({includes:'ModuleSpringUsage', severity:'LOW'}).should(r => {
            expect(r.status).to.eq(200);
            expect(r.status.health).to.eq('GREEN');
        });

        cy.installBundle('springUsageModuleProbe/module-without-spring-3.3.0-SNAPSHOT.jar');
        cy.runProvisioningScript([{startBundle: 'module-without-spring/3.3.0.SNAPSHOT'}]);
        healthCheck({includes:'ModuleSpringUsage', severity:'LOW'}).should(r => {
            expect(r.status).to.eq(200);
            expect(r.status.health).to.eq('GREEN');
        });

        cy.uninstallBundle('module-without-spring');
    });

    //TODO Test 2: deploy a module that uses spring but jahia provided and check that the probe is still green,
    //TODO Test 3: check the probe is green, change config to include jahia module and check that probes become yellow, restore config
    //TODO Test 4: check the probe is green, deploy a non jahia module that uses spring with beans.xml files (no import) and check that the probe is yellow, undeploy module
    //TODO Test 5: check the probe is green, deploy a non jahia module that uses spring in imports and check that the probe is yellow, undeploy module

});
