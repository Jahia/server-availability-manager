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

        healthCheck({severity: 'LOW'}).should(r => {
            expect(r.status.health).to.eq('GREEN');
            expect(r.probes.length).to.be.gte(6);
        });
    });

    it('Filters with "health" parameter', () => {
        cy.runProvisioningScript({fileName: 'test-enable.json'});

        cy.waitUntil(() => cy.task('sshCommand', sshCommands)
            .then(waitUntilTestFcnEnable), waitUntilOptions);

        console.log('Return with blank filter');
        healthCheck({severity: 'LOW'}).should(r => {
            expect(r.probes.length).to.be.gte(6);
        });

        console.log('Return with green filter');
        healthCheck({severity: 'LOW', health: 'GREEN'}).should(r => {
            expect(r.probes.length).to.be.gte(6);
        });

        console.log('Return with yellow filter');
        healthCheck({severity: 'LOW', health: 'YELLOW'}).should(r => {
            expect(r.probes.length).to.be.gte(1);
        });

        console.log('Return with red filter');
        healthCheck({severity: 'LOW', health: 'RED'}).should(r => {
            expect(r.probes.length).to.be.gte(1);
        });
    });

    it('Filters with "includes" parameter', () => {
        cy.runProvisioningScript({fileName: 'test-disable.json'});

        cy.waitUntil(() => cy.task('sshCommand', sshCommands)
            .then(waitUntilTestFcnDisable), waitUntilOptions);

        console.log('Return with blank filter');
        healthCheck({severity: 'LOW', includes: []}).should(r => {
            expect(r.probes).to.be.empty;
        });

        console.log('Return one probe');
        healthCheck({severity: 'LOW', includes: 'FileDatastore'}).should(r => {
            expect(r.probes.length).to.be.eq(1);
            expect(r.probes[0].name).to.be.eq('FileDatastore');
        });

        console.log('Return more than one probe');
        healthCheck({severity: 'LOW', includes: ['FileDatastore', 'DBConnectivity']}).should(r => {
            expect(r.probes.length).to.be.eq(2);
            const probeNames = r.probes?.map(p => p.name);
            expect('FileDatastore').to.be.oneOf(probeNames);
            expect('DBConnectivity').to.be.oneOf(probeNames);
        });

        console.log('Filter with only invalid probe');
        healthCheck({severity: 'LOW', includes: ['UndefinedProbe']}).should(r => {
            expect(r.probes).to.be.empty;
        });

        console.log('Filter invalid probe');
        healthCheck({severity: 'LOW', includes: ['FileDatastore', 'UndefinedProbe']}).should(r => {
            expect(r.probes.length).to.be.eq(1);
            expect(r.probes[0].name).to.be.eq('FileDatastore');
        });
    });

    it('Check healthcheck with one HIGH probe RED, asking low', () => {
        cy.runProvisioningScript({fileName: 'test-enable.json'});

        cy.waitUntil(() => cy.task('sshCommand', sshCommands)
            .then(waitUntilTestFcnEnable), waitUntilOptions);

        healthCheck({severity: 'LOW'}).should(r => {
            expect(r.status.health).to.eq('RED');
            expect(r.probes.length).to.be.gte(7);
        });
    });

    it('Check healthcheck with one HIGH probe RED, asking critical', () => {
        cy.runProvisioningScript({fileName: 'test-enable.json'});

        cy.waitUntil(() => cy.task('sshCommand', sshCommands)
            .then(waitUntilTestFcnEnable), waitUntilOptions);

        healthCheck({severity: 'CRITICAL'}).should(r => {
            expect(r.status.health).to.eq('GREEN');
            expect(r.probes.length).to.eq(4);
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
            expect(response.status).to.eq(200);
            expect(response.body.status.health).to.eq('GREEN');
            expect(response.body.probes.length).to.be.gte(6);
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
            expect(response.body.probes.length).to.eq(4);
            expect(response.status).to.eq(200);
        });
    });

    it('Check Probe coming from another bundle is accessible', () => {
        cy.runProvisioningScript({fileName: 'test-disable.json'});

        cy.waitUntil(() => cy.task('sshCommand', sshCommands)
            .then(waitUntilTestFcnDisable), waitUntilOptions);

        cy.log('Should find the DummyState probe as the module is started');
        healthCheck({severity: 'LOW'}).should(r => {
            const dummyProbe = r.probes.find(probe => probe.name === 'DummyState');
            expect(dummyProbe).to.exist;
            expect(dummyProbe.status.health).to.eq('GREEN');
        });

        cy.log('Should not find the DummyState probe as the module is not started');
        cy.runProvisioningScript({fileName: 'moduleStateProbe/stop-bundle.json', replacements: {BUNDLE_NAME: 'server-availability-manager-test-module'}});
        healthCheck({severity: 'LOW'}).should(r => {
            const dummyProbe = r.probes.find(probe => probe.name === 'DummyState');
            expect(dummyProbe).to.not.exist;
        });

        cy.log('Should find again the DummyState probe as the module has been started again');
        cy.runProvisioningScript({fileName: 'moduleStateProbe/start-bundle.json', replacements: {BUNDLE_NAME: 'server-availability-manager-test-module'}});
        healthCheck({severity: 'LOW'}).should(r => {
            const dummyProbe = r.probes.find(probe => probe.name === 'DummyState');
            expect(dummyProbe).to.exist;
            expect(dummyProbe.status.health).to.eq('GREEN');
        });
    });

    it('Checks if RenderingChain is green', () => {
        healthCheck({severity: 'CRITICAL', includes: ['RenderingChain']}).should(r => {
            expect(r.status.health).to.eq('GREEN');
            expect(r.probes.length).to.be.eq(1);
        });
    });
});
