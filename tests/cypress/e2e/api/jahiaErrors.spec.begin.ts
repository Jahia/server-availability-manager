import {healthCheck} from '../../support/gql';

describe('Jahia errors probe test', () => {
    it('Checks that JahiaErrors probe is functional', () => {
        healthCheck({severity: 'DEBUG'}).then(r => {
            cy.log(JSON.stringify(r));
            const jahiaErrorsProbe = r.probes.find(probe => probe.name === 'JahiaErrors');
            expect(jahiaErrorsProbe.description).to.contain('Count the number of errors faced by Jahia');
            expect(jahiaErrorsProbe.severity).to.eq('DEBUG');

            if (r.status.health === 'YELLOW') {
                expect(jahiaErrorsProbe.status.message).to.contain('errors are present on the platform, errors are not expected in a production environment and we recommend reviewing these.');
            } else {
                expect(jahiaErrorsProbe.status.message).to.contain('No errors are present on the platform');
            }
        });
    });

    it('Check that Jahia errors probe is YELLOW when there is an error log', () => {
        cy.executeGroovy('groovy/simpleErrorLog.groovy');
        healthCheck({severity: 'DEBUG'}).then(r => {
            expect(r.status.health).to.eq('YELLOW');
            const jahiaErrorsProbe = r.probes.find(probe => probe.name === 'JahiaErrors');
            expect(jahiaErrorsProbe.status.health).to.eq('YELLOW');
            expect(jahiaErrorsProbe.severity).to.eq('DEBUG');
            // Hard to predict exact number of errors
            expect(jahiaErrorsProbe.status.message).to.contain('errors are present on the platform, errors are not expected in a production environment and we recommend reviewing these.');
        });
    });

    it('Check that Jahia errors probe is YELLOW when there is a fatal log', () => {
        cy.executeGroovy('groovy/simpleFatalLog.groovy');
        healthCheck({severity: 'DEBUG'}).then(r => {
            expect(r.status.health).to.eq('YELLOW');
            const jahiaErrorsProbe = r.probes.find(probe => probe.name === 'JahiaErrors');
            expect(jahiaErrorsProbe.status.health).to.eq('YELLOW');
            expect(jahiaErrorsProbe.severity).to.eq('DEBUG');
            // Hard to predict exact number of errors
            expect(jahiaErrorsProbe.status.message).to.contains('errors are present on the platform, errors are not expected in a production environment and we recommend reviewing these.');
        });
    });

    // Hard to guarantee we actually get a clean log on snapshot
    // it('Check that Jahia errors probe is not present when severity equals to LOW', () => {
    //     healthCheck({severity: 'LOW'}).then(r => {
    //         cy.log(JSON.stringify(r));
    //         cy.then(() => expect(r.status.health).to.eq('GREEN'));
    //         const jahiaErrorsProbe = r.probes.find(probe => probe.name === 'JahiaErrors');
    //         expect(jahiaErrorsProbe).to.be.undefined;
    //     });
    // });
});
