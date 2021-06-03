/* eslint-disable @typescript-eslint/no-explicit-any */
// import { apollo } from '../../support/apollo'
import { DocumentNode } from 'graphql'

describe('Shutdown via API - mutation.admin.serverAvailabilityManager.shutdown', () => {
    let GQL_SHUTDOWN: DocumentNode

    before('load graphql file and create test dataset', () => {
        GQL_SHUTDOWN = require(`graphql-tag/loader!../../fixtures/shutdown.graphql`)
    })

    it('Shutdown success', function () {
        //This test must be the last test for obviously reason
        cy.task('apolloNode', {
            baseUrl: Cypress.config().baseUrl,
            authMethod: { username: Cypress.env('JAHIA_USERNAME'), password: Cypress.env('JAHIA_PASSWORD') },
            mode: 'mutate',
            variables: {
                timeout: 100,
            },
            query: GQL_SHUTDOWN,
        }).then((response: any) => {
            cy.log(JSON.stringify(response))
            expect(response.data.admin.serverAvailabilityManager.shutdown).to.be.true
        })
    })
})
