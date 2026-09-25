import {healthCheckAPI as healthcheck} from '../../support/utils';

describe('healthcheck REST API test', () => {
    const probesConfig = 'config:list "(service.pid=org.jahia.modules.sam.healthcheck.ProbesRegistry)"';
    const waitUntilOptions = {interval: 250, timeout: 5000, errorMsg: 'The probe configuration did not land'};

    // The endpoint declares a Content-Length. It used to count characters, and a character that takes more than
    // one byte then made the declared length shorter than the body, so the response was cut and did not parse.
    // The test probe carries such a character through its configurable message, so no second bundle is needed.
    //
    // The message is set over ssh and not through the provisioning script. That script is sent with btoa, which
    // encodes the character as one Latin-1 byte, and the server decodes it as UTF-8, so it arrives as U+FFFD.
    // The wait matches the ASCII marker only, because the console renders the character as "?" either way.
    it('serves the whole response when a probe message carries a multi-byte character', () => {
        cy.runProvisioningScript({fileName: 'test-multibyte.json'});
        cy.task('sshCommand', [
            'config:edit org.jahia.modules.sam.healthcheck.ProbesRegistry',
            'config:property-set probes.testProbe.message "multibyte-marker caf\u00e9"',
            'config:update'
        ]);
        cy.waitUntil(() => cy.task('sshCommand', [probesConfig])
            .then((out: string) => out.includes('multibyte-marker')), waitUntilOptions);

        healthcheck({includes: 'testProbe'}).should(response => {
            // A truncated body does not parse, so response.body stays a string. Naming that first makes the
            // regression report its own cause instead of a type error.
            expect(response.status).to.eq(200);
            expect(response.body.probes).to.be.an('array').and.have.length(1);
            expect(response.body.probes[0].status.message).to.eq('multibyte-marker caf\u00e9');
        });

        cy.runProvisioningScript({fileName: 'test-multibyte-clear.json'});
        cy.waitUntil(() => cy.task('sshCommand', [probesConfig])
            .then((out: string) => !out.includes('multibyte-marker')), waitUntilOptions);
    });

    it('should return using "includes" parameter', () => {
        console.log('Return with no filter');
        healthcheck({}).should(response => {
            expect(response.status).to.eq(200);
            expect(response.body.probes).to.not.be.empty;
        });

        console.log('Return with empty filter');
        healthcheck({includes: undefined}).should(response => {
            expect(response.status).to.eq(200);
            expect(response.body.probes).to.not.be.empty;
        });

        console.log('Return one probe');
        healthcheck({includes: 'FileDatastore'}).should(response => {
            expect(response.status).to.eq(200);
            expect(response.body.probes?.length).to.eq(1);
            expect(response.body.probes[0]?.name).to.eq('FileDatastore');
        });

        console.log('Return more than one probe');
        healthcheck({includes: 'FileDatastore,DBConnectivity'}).should(response => {
            expect(response.status).to.eq(200);
            expect(response.body.probes.length).to.be.eq(2);
            const probeNames = response.body.probes?.map(r => r.name);
            expect('FileDatastore').to.be.oneOf(probeNames);
            expect('DBConnectivity').to.be.oneOf(probeNames);
        });

        console.log('Filter with only invalid probe');
        healthcheck({includes: 'UndefinedProbe'}).should(response => {
            expect(response.status).to.eq(200);
            expect(response.body.probes).to.be.empty;
        });

        console.log('Filter invalid probe');
        healthcheck({includes: 'FileDatastore,UndefinedProbe'}).should(response => {
            expect(response.status).to.eq(200);
            expect(response.body.probes.length).to.be.eq(1);
            const probeNames = response.body.probes?.map(r => r.name);
            expect('FileDatastore').to.be.oneOf(probeNames);
        });
    });
});
