import {healthCheck} from '../../support/gql';

describe('Jahia errors probe test', () => {
    const sshCommands = [
        'config:list "(service.pid=org.jahia.modules.sam.healthcheck.ProbesRegistry)"'
    ];

    const waitUntilOptions = {
        interval: 250,
        timeout: 5000,
        errorMsg: 'Failed to verify configuration update'
    };

    const waitUntilTestFcnDisable = (response: string) => response.indexOf('probes.JahiaErrors.severity = IGNORED') !== -1;

    after(() => {
        cy.runProvisioningScript({fileName: 'jahiaErrorsProbe/disable.json'});

        cy.waitUntil(() => cy.task('sshCommand', sshCommands)
            .then(waitUntilTestFcnDisable), waitUntilOptions);
    });

    it('Check the description of the probe', () => {
        healthCheck('LOW').then(r => {
            cy.log(JSON.stringify(r));
            cy.then(() => expect(r.status.health).to.eq('GREEN'));
            const jahiaErrorsProbe = r.probes.find(probe => probe.name === 'JahiaErrors');
            expect(jahiaErrorsProbe.description).to.eq('Count the number of errors faced by Jahia');
        });
    });

    it('Check that Jahia errors probe is present with GREEN status', () => {
        healthCheck('LOW').then(r => {
            cy.log(JSON.stringify(r));
            cy.then(() => expect(r.status.health).to.eq('GREEN'));
            const jahiaErrorsProbe = r.probes.find(probe => probe.name === 'JahiaErrors');
            expect(jahiaErrorsProbe.status.health).to.eq('GREEN');
            expect(jahiaErrorsProbe.severity).to.eq('LOW');
        });
    });

    it('Check that Jahia errors probe is YELLOW when there is an error log', () => {
        cy.executeGroovy('groovy/simpleErrorLog.groovy');
        healthCheck('LOW').then(r => {
            expect(r.status.health).to.eq('YELLOW');
            const jahiaErrorsProbe = r.probes.find(probe => probe.name === 'JahiaErrors');
            expect(jahiaErrorsProbe.status.health).to.eq('YELLOW');
            expect(jahiaErrorsProbe.severity).to.eq('LOW');
            expect(jahiaErrorsProbe.status.message).to.eq('A total of 1 errors are present on the platform, errors are not expected in a production environment and we recommend reviewing these.');
        });
    });

    it('Check that Jahia errors probe is not present when severity equals to MEDIUM', () => {
        healthCheck('MEDIUM').then(r => {
            cy.log(JSON.stringify(r));
            cy.then(() => expect(r.status.health).to.eq('GREEN'));
            const jahiaErrorsProbe = r.probes.find(probe => probe.name === 'JahiaErrors');
            expect(jahiaErrorsProbe).to.be.undefined;
        });
    });
});
