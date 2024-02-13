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

        healthcheck().should(response => {
            expect(response.body.status.health).to.eq('GREEN');
            expect(response.status).to.eq(200);
        });

        cy.installBundle('moduleDefinitionsProbe/test-1.1-SNAPSHOT.jar');
        cy.runProvisioningScript([{startBundle: 'test/1.1.0.SNAPSHOT'}]);

        healthcheck().should(response => {
            expect(response.body.status.health).to.eq('YELLOW');
            expect(response.status).to.eq(200);
        });

        cy.runProvisioningScript([{startBundle: 'test/1.0.0.SNAPSHOT'}]);

        healthcheck().should(response => {
            expect(response.body.status.health).to.eq('RED');
            expect(response.status).to.eq(503);
        });
    });

    afterEach(() => {
        cy.login();
        cy.visit('/tools/osgi/console/bundles');
        cy.get('.filter').first().type('test');
        cy.get('.filterApply').first().click();
        cy.get('.ui-icon-trash').each($el => {
            cy.wrap($el).click();
        });
        // Cy.get('.ui-icon-trash', {timeout: 2000}).click({multiple: true, force: true});
        cy.log('Cleared previously installed test bundles');
    });
});
