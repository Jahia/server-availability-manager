const healthcheck = () => {
    return cy.request({
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
    });
};

describe('Module definitions probe test', () => {
    it('should fail when installing incompatible definitions', function () {
        cy.installBundle('moduleDefinitionsProbe/test-1.0-SNAPSHOT.jar');
        cy.runProvisioningScript([{startBundle: 'test/1.0.0.SNAPSHOT'}]);
        cy.installBundle('moduleDefinitionsProbe/test-1.2-SNAPSHOT.jar');

        cy.login();
        cy.visit('/tools/osgi/console/bundles');
        cy.get('.filter').first().type('test');
        cy.get('.filterApply').first().click();
        cy.get('td').contains('1.0.0.SNAPSHOT');
        cy.get('td').contains('1.2.0.SNAPSHOT').should('not.exist');
    });

    it('show an error if running version has incompatible definitions', function () {
        cy.installBundle('moduleDefinitionsProbe/test-1.0-SNAPSHOT.jar');
        cy.runProvisioningScript([{startBundle: 'test/1.0.0.SNAPSHOT'}]);

        cy.waitUntil(() => healthcheck()
            .then(response => {
                if (response?.body?.status?.health) {
                    return response;
                }

                return false;
            }), {
            interval: 1000,
            timeout: 10000,
            errorMsg: 'Failed to read healthcheck (GREEN)'
        });

        healthcheck().then(response => {
            expect(response.body.status.health).to.eq('GREEN');
            expect(response.status).to.eq(200);
        });

        cy.installBundle('moduleDefinitionsProbe/test-1.1-SNAPSHOT.jar');
        cy.runProvisioningScript([{startBundle: 'test/1.1.0.SNAPSHOT'}]);

        cy.waitUntil(() => healthcheck()
            .then(response => {
                if (response?.body?.status?.health) {
                    return response;
                }

                return false;
            }), {
            interval: 1000,
            timeout: 10000,
            errorMsg: 'Failed to read healthcheck (YELLOW)'
        });

        healthcheck().should(response => {
            expect(response.body.status.health).to.eq('YELLOW');
            expect(response.status).to.eq(200);
        });

        cy.runProvisioningScript([{startBundle: 'test/1.0.0.SNAPSHOT'}]);

        cy.waitUntil(() => healthcheck()
            .then(response => {
                if (response?.body?.status?.health) {
                    return response;
                }

                return false;
            }), {
            interval: 1000,
            timeout: 10000,
            errorMsg: 'Failed to read healthcheck (RED)'
        });
        healthcheck().should(response => {
            expect(response.body.status.health).to.eq('RED');
            expect(response.status).to.eq(503);
        });
    });

    afterEach(function () {
        cy.login();
        cy.visit('/tools/osgi/console/bundles');
        cy.get('.filter').first().type('test');
        cy.get('.filterApply').first().click();
        cy.get('.ui-icon-trash', {timeout: 500}).click({multiple: true});
    });
});
