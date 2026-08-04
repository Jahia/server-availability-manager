import {healthCheckAPI as healthcheck} from '../../support/utils';

describe('Module definitions probe test', () => {
    it('should fail when installing incompatible definitions', {retries: 5}, function () {
        cy.login();
        cy.installBundle('moduleDefinitionsProbe/test-1.0-SNAPSHOT.jar');
        cy.runProvisioningScript([{startBundle: 'test/1.0.0.SNAPSHOT'}]);
        healthcheck({}).should(response => {
            expect(response.body.status.health).to.eq('GREEN');
            expect(response.status).to.eq(200);
        });
        cy.installBundle('moduleDefinitionsProbe/test-1.2-SNAPSHOT.jar');

        cy.visit('/tools/osgi/console/bundles');
        // Scope the check to the "test" bundle's own row: the page lists every bundle
        // installed on the instance, so asserting on unscoped `td` content is fragile
        // whenever another bundle's version happens to collide with '1.2.0.SNAPSHOT'.
        cy.contains('.symName', /^test$/).closest('tr').within(() => {
            cy.contains('td', '1.0.0.SNAPSHOT');
            cy.contains('td', '1.2.0.SNAPSHOT').should('not.exist');
        });
    });

    it('show an error if running version has incompatible definitions', function () {
        cy.installBundle('moduleDefinitionsProbe/test-1.0-SNAPSHOT.jar');
        cy.runProvisioningScript([{startBundle: 'test/1.0.0.SNAPSHOT'}]);

        healthcheck({}).should(response => {
            expect(response.body.status.health).to.eq('GREEN');
            expect(response.status).to.eq(200);
        });

        cy.installBundle('moduleDefinitionsProbe/test-1.1-SNAPSHOT.jar');
        cy.runProvisioningScript([{startBundle: 'test/1.1.0.SNAPSHOT'}]);

        healthcheck({}).should(response => {
            expect(response.body.status.health).to.eq('YELLOW');
            expect(response.status).to.eq(200);
        });

        cy.runProvisioningScript([{startBundle: 'test/1.0.0.SNAPSHOT'}]);

        healthcheck({}).should(response => {
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
