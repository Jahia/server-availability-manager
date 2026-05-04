/* eslint-disable @typescript-eslint/no-explicit-any */
import Chainable = Cypress.Chainable

type HealthCheckQuery = Record<string, string | number | boolean | null | undefined>;

/**
 * Executes a call to healthcheck REST API endpoint
 * @param qs
 */
export const healthCheckAPI = (qs?: HealthCheckQuery): Chainable<Cypress.Response<any>> => {
    return cy.request({
        url: `${Cypress.config().baseUrl}/modules/healthcheck`,
        qs,
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
