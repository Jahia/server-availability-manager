import { DocumentNode } from 'graphql'
import { apollo } from '../../support/apollo'

describe('Shutdown via API - mutation.admin.serverAvailabilityManager.shutdown', () => {
    let GQL_SHUTDOWN: DocumentNode

    before('load graphql file and create test dataset', () => {
        GQL_SHUTDOWN = require(`graphql-tag/loader!../../fixtures/shutdown.graphql`)
    })

    it('Shutdown success', function () {
        //This test must be the last test for obviously reason
        cy.apolloMutate(apollo(), {
            variables: {
                timeout: 100,
            },
            mutation: GQL_SHUTDOWN,
        })
            .its('data.admin.serverAvailabilityManager.shutdown')
            .should('eq', true)
    })
})
