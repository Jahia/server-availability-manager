import {healthCheck} from '../../support/gql';

// This test is currently ignored as it often fails in snapshot due to its nature.
describe('Jahia errors probe test', () => {
    it('Check the description of the probe', () => {
        healthCheck({severity: 'DEBUG'}).then(r => {
            cy.log(JSON.stringify(r));
            cy.then(() => expect(r.status.health).to.eq('GREEN'));
            const jahiaErrorsProbe = r.probes.find(probe => probe.name === 'JahiaErrors');
            expect(jahiaErrorsProbe.description).to.contain('Count the number of errors faced by Jahia');
        });
    });

    it('Check that Jahia errors probe is present with GREEN status', () => {
        healthCheck({severity: 'DEBUG'}).then(r => {
            cy.log(JSON.stringify(r));
            cy.then(() => expect(r.status.health).to.eq('GREEN'));
            const jahiaErrorsProbe = r.probes.find(probe => probe.name === 'JahiaErrors');
            expect(jahiaErrorsProbe.status.health).to.eq('GREEN');
            expect(jahiaErrorsProbe.severity).to.eq('DEBUG');
        });
    });

    it('Check that Jahia errors probe is YELLOW when there is an error log', () => {
        cy.executeGroovy('groovy/simpleErrorLog.groovy');
        healthCheck({severity: 'DEBUG'}).then(r => {
            expect(r.status.health).to.eq('YELLOW');
            const jahiaErrorsProbe = r.probes.find(probe => probe.name === 'JahiaErrors');
            expect(jahiaErrorsProbe.status.health).to.eq('YELLOW');
            expect(jahiaErrorsProbe.severity).to.eq('DEBUG');
            expect(jahiaErrorsProbe.status.message).to.eq('A total of 1 errors are present on the platform, errors are not expected in a production environment and we recommend reviewing these.');
        });
    });

    it('Check that Jahia errors probe is YELLOW when there is a fatal log', () => {
        cy.executeGroovy('groovy/simpleFatalLog.groovy');
        healthCheck({severity: 'DEBUG'}).then(r => {
            expect(r.status.health).to.eq('YELLOW');
            const jahiaErrorsProbe = r.probes.find(probe => probe.name === 'JahiaErrors');
            expect(jahiaErrorsProbe.status.health).to.eq('YELLOW');
            expect(jahiaErrorsProbe.severity).to.eq('DEBUG');
            expect(jahiaErrorsProbe.status.message).to.match(/A total of ([12]) errors are present on the platform, errors are not expected in a production environment and we recommend reviewing these./);
        });
    });

    it('Check that Jahia errors probe is not present when severity equals to LOW', () => {
        healthCheck({severity: 'LOW'}).then(r => {
            cy.log(JSON.stringify(r));
            cy.then(() => expect(r.status.health).to.eq('GREEN'));
            const jahiaErrorsProbe = r.probes.find(probe => probe.name === 'JahiaErrors');
            expect(jahiaErrorsProbe).to.be.undefined;
        });
    });
});
