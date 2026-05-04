import {healthCheck as healthCheckGQL} from '../../support/gql';
import {healthCheckAPI} from "../../support/utils";

const API_SECURITY_CONFIG_DEFAULT = 'securityProfiles/security-profile-default.json';
const API_SECURITY_CONFIG_OFF = 'securityProfiles/security-profile-off.json';
const API_PROVISIONING_ACCESS_GRANT = 'securityProfiles/provisioning-access-grant.json';
const API_PROVISIONING_ACCESS_REMOVE = 'securityProfiles/provisioning-access-remove.json';

/**
 * Cover the edge case when the security profile is set to "off", which means that default GraphQL API scopes are not defined.
 * In such case SAM healthcheck servlet should still be accessible, because corresponding access is granted
 * in SAM configuration org.jahia.bundles.api.authorization-sam.yml, but direct GraphQL API access should be denied.
 *
 * See https://github.com/Jahia/jahia-private/issues/4702 and fix for SAM https://github.com/Jahia/server-availability-manager/pull/238
 */
describe('healthcheck REST API test with security.profile=off', () => {
    before(() => {
        // Grant provisioning API access through the SAM configuration.
        // Required to be able to restore default configuration aftr the test.
        cy.runProvisioningScript({fileName: API_PROVISIONING_ACCESS_GRANT});
        // Turn security.profile off to remove default GraphQL API scopes
        cy.runProvisioningScript({fileName: API_SECURITY_CONFIG_OFF});
        // eslint-disable-next-line cypress/no-unnecessary-waiting
        cy.wait(1000); // Wait for the configuration to be applied.
    });

    after(() => {
        // Restore default configuration
        cy.runProvisioningScript({fileName: API_SECURITY_CONFIG_DEFAULT});
        cy.runProvisioningScript({fileName: API_PROVISIONING_ACCESS_REMOVE});
    });

    it('GraphQL endpoint should deny an access (since graphql scope is not granted)', () => {
        healthCheckGQL({severity: 'LOW', health: 'GREEN'}).should(response => {
            expect(response.errors?.[0]?.errorType, 'Access to GraphQL should be denied').to.be.equal('GqlAccessDeniedException');
            expect(response.data, 'Response data should be null').to.be.null;
        });
    });

    it('REST API endpoint should return probes data (using API scopes from SAM config)', () => {
        healthCheckAPI({}).should(response => {
            expect(response.status).to.eq(200);
            expect(response.body.probes).to.not.be.empty;
        });
    });
});
