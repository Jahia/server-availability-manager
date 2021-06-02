/* eslint-disable @typescript-eslint/no-explicit-any */
import { apollo } from '../../support/apollo'
import { DocumentNode } from 'graphql'

describe('Shutdown via API - mutation.admin.serverAvailabilityManager.shutdown', () => {
    let GQL_SHUTDOWN: DocumentNode

    before('load graphql file and create test dataset', () => {
        GQL_SHUTDOWN = require(`graphql-tag/loader!../../fixtures/shutdown.graphql`)
    })
    it('Should fail Shutdown wrong timeout format', function () {
        cy.task('apolloNode', {
            baseUrl: Cypress.config().baseUrl,
            authMethod: { username: Cypress.env('JAHIA_USERNAME'), password: Cypress.env('JAHIA_PASSWORD') },
            mode: 'mutate',
            variables: {
                timeout: 'ABC',
            },
            query: GQL_SHUTDOWN,
        }).then(async (response: any) => {
            cy.log(JSON.stringify(response))
            expect(response.graphQLErrors[0].message).to.contain('Internal Server Error(s) while executing query')
        })
    })
    // it('Shutdown success', function () {
    //     //This test must be the last test for obviously reason
    //     cy.task('apolloNode', {
    //         baseUrl: Cypress.config().baseUrl,
    //         authMethod: { username: Cypress.env('JAHIA_USERNAME'), password: Cypress.env('JAHIA_PASSWORD') },
    //         mode: 'mutate',
    //         variables: {
    //             timeout: 100,
    //         },
    //         query: GQL_SHUTDOWN,
    //     }).then(async (response: any) => {
    //         cy.log(JSON.stringify(response))
    //         // expect(response.data.admin.serverAvailabilityManager.shutdown).to.be.true
    //     })
    // })
    // it('Shutdown success away method', async function () {
    //     const response = await apollo().query({
    //         query: GQL_SHUTDOWN,
    //         variables: {
    //             timeout: 10,
    //         },
    //     })
    //     cy.log(JSON.stringify(response))
    // })
})
