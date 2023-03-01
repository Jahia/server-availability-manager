import {healthCheck} from '../../support/gql';

describe('Health check', () => {
    const sshCommands = [
        'config:list "(service.pid=org.jahia.modules.sam.healthcheck.ProbesRegistry)"'
    ];

    const waitUntilOptions = {
        interval: 250,
        timeout: 5000,
        errorMsg: 'Failed to verify configuration update'
    };

    const waitUntilTestFcnDisable = (response: string) => response.indexOf('probes.testProbe.severity = IGNORED') !== -1 && response.indexOf('probes.testProbe.status = GREEN') !== -1;
    const waitUntilTestFcnEnable = (response: string) => response.indexOf('probes.testProbe.severity = HIGH') !== -1 && response.indexOf('probes.testProbe.status = RED') !== -1;

    after(() => {
        cy.runProvisioningScript({fileName: 'test-disable.json'});

        cy.waitUntil(() => cy.task('sshCommand', sshCommands)
            .then(waitUntilTestFcnDisable), waitUntilOptions);
    });

    it('Check healthcheck when everything is fine', () => {
        cy.runProvisioningScript({fileName: 'test-disable.json'});

        cy.waitUntil(() => cy.task('sshCommand', sshCommands)
            .then(waitUntilTestFcnDisable), waitUntilOptions);

        healthCheck('LOW').should(r => {
            expect(r.status.health).to.eq('GREEN');
            expect(r.probes.length).to.be.gte(6);
        });
    });

    it('Check healthcheck with one HIGH probe RED, asking low', () => {
        cy.runProvisioningScript({fileName: 'test-enable.json'});

        cy.waitUntil(() => cy.task('sshCommand', sshCommands)
            .then(waitUntilTestFcnEnable), waitUntilOptions);

        healthCheck('LOW').should(r => {
            expect(r.status.health).to.eq('RED');
            expect(r.probes.length).to.be.gte(7);
        });
    });

    it('Check healthcheck with one HIGH probe RED, asking critical', () => {
        cy.runProvisioningScript({fileName: 'test-enable.json'});

        cy.waitUntil(() => cy.task('sshCommand', sshCommands)
            .then(waitUntilTestFcnEnable), waitUntilOptions);

        healthCheck('CRITICAL').should(r => {
            expect(r.status.health).to.eq('GREEN');
            expect(r.probes.length).to.eq(3);
        });
    });

    it('Check healthcheck servlet when everything is fine', () => {
        cy.runProvisioningScript({fileName: 'test-disable.json'});

        cy.waitUntil(() => cy.task('sshCommand', sshCommands)
            .then(waitUntilTestFcnDisable), waitUntilOptions);

        cy.request({
            url: `${Cypress.config().baseUrl}/modules/healthcheck`,
            headers: {
                referer: Cypress.config().baseUrl
            },
            auth: {
                user: 'root',
                pass: Cypress.env('SUPER_USER_PASSWORD'),
                sendImmediately: true
            }
        }).should(response => {
            expect(response.body.status.health).to.eq('GREEN');
            expect(response.body.probes.length).to.be.gte(6);
            expect(response.status).to.eq(200);
        });
    });

    it('Check healthcheck servlet with one HIGH probe RED, default severity, should return 503', () => {
        cy.runProvisioningScript({fileName: 'test-enable.json'});

        cy.waitUntil(() => cy.task('sshCommand', sshCommands)
            .then(waitUntilTestFcnEnable), waitUntilOptions);

        cy.request({
            url: `${Cypress.config().baseUrl}/modules/healthcheck`,
            headers: {
                referer: Cypress.config().baseUrl
            },
            auth: {
                user: 'root',
                pass: Cypress.env('SUPER_USER_PASSWORD'),
                sendImmediately: true
            },
            failOnStatusCode: false
        }).should(response => {
            expect(response.body.status.health).to.eq('RED');
            expect(response.body.probes.length).to.be.gte(7);
            expect(response.status).to.eq(503);
        });
    });

    it('Check healthcheck servlet with one HIGH probe RED, asking critical', () => {
        cy.runProvisioningScript({fileName: 'test-enable.json'});

        cy.waitUntil(() => cy.task('sshCommand', sshCommands)
            .then(waitUntilTestFcnEnable), waitUntilOptions);

        cy.request({
            url: `${Cypress.config().baseUrl}/modules/healthcheck?severity=critical`,
            headers: {
                referer: Cypress.config().baseUrl
            },
            auth: {
                user: 'root',
                pass: Cypress.env('SUPER_USER_PASSWORD'),
                sendImmediately: true
            }
        }).should(response => {
            expect(response.body.status.health).to.eq('GREEN');
            expect(response.body.probes.length).to.eq(3);
            expect(response.status).to.eq(200);
        });
    });
});
