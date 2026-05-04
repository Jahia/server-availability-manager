import {healthCheck as healthCheckGQL} from '../../support/gql';
import {healthCheckAPI} from '../../support/utils';

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
        // Required to be able to restore default configuration after the test.
        cy.runProvisioningScript({fileName: API_PROVISIONING_ACCESS_GRANT});
        // Turn security.profile off to remove default GraphQL API scopes
        cy.runProvisioningScript({fileName: API_SECURITY_CONFIG_OFF});
        // Once security.profile is turned OFF, default access to GraphQL should be denied.
        // Wait for default profile to be emptied before making sure SAM scopes grant all required access.
        cy.waitUntil(() =>
                healthCheckGQL({severity: 'LOW', health: 'GREEN'}).then(response => {
                    return response.errors?.[0]?.errorType === 'GqlAccessDeniedException';
                }),
            {
                timeout: 10_000,
                interval: 500,
                errorMsg: 'Timed out waiting for security.profile=off to deny GraphQL access',
            }
        );
    });

    after(() => {
        // Restore default configuration
        cy.runProvisioningScript({fileName: API_SECURITY_CONFIG_DEFAULT});
        cy.runProvisioningScript({fileName: API_PROVISIONING_ACCESS_REMOVE});
    });

    it('REST API endpoint should return probes data (SAM API scopes should grant required access)', () => {
        healthCheckAPI({}).should(response => {
            expect(response.status, 'Access to healthcheck REST endpoint should be granted').to.eq(200);
            expect(response.body.probes, 'Healthcheck probes list should not be empty').to.not.be.empty;
        });
    });
});
