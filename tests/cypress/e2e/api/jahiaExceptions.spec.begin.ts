import {healthCheck} from '../../support/gql';

describe('Jahia exceptions probe test', () => {

    const sshCommands = [
        'config:list "(service.pid=org.jahia.modules.sam.healthcheck.ProbesRegistry)"'
    ];

    const waitUntilOptions = {
        interval: 250,
        timeout: 5000,
        errorMsg: 'Failed to verify configuration update'
    };

    const waitUntilTestFcnDisable = (response: string) => response.indexOf('probes.JahiaExceptions.severity = IGNORED') !== -1;
    const waitUntilTestFcnEnable = (response: string) => response.indexOf('probes.JahiaExceptions.severity = LOW') !== -1

    before('Activate the probe', () => {
        cy.runProvisioningScript({fileName: 'jahiaExceptionsProbe/enable.json'});

        cy.waitUntil(() => cy.task('sshCommand', sshCommands)
            .then(waitUntilTestFcnEnable), waitUntilOptions);
    })

    after(() => {
        cy.runProvisioningScript({fileName: 'jahiaExceptionsProbe/disable.json'});

        cy.waitUntil(() => cy.task('sshCommand', sshCommands)
            .then(waitUntilTestFcnDisable), waitUntilOptions);
    });

    it('Check that Jahia exceptions probe is present with GREEN status', () => {
        healthCheck('LOW').should(r => {
            expect(r.status.health).to.eq('GREEN');
            const moduleStateProbe = r.probes.find(probe => probe.name === 'JahiaExceptions');
            expect(moduleStateProbe.status.health).to.eq('GREEN');
            expect(moduleStateProbe.severity).to.eq('LOW');
        });
    });

    it('Check that Jahia exceptions probe is YELLOW when there is an error log', () => {
        cy.executeGroovy('groovy/simpleErrorLog.groovy');
        healthCheck('LOW').should(r => {
            expect(r.status.health).to.eq('YELLOW');
            const moduleStateProbe = r.probes.find(probe => probe.name === 'JahiaExceptions');
            expect(moduleStateProbe.status.health).to.eq('YELLOW');
            expect(moduleStateProbe.severity).to.eq('LOW');
            expect(moduleStateProbe.status.message).to.eq('A total of 1 exceptions are present on the platform, exceptions are not expected in a production environment and we recommend reviewing these.');
        });
    });
});
