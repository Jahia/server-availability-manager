/* eslint-disable @typescript-eslint/no-explicit-any */
import { apollo } from '../../support/apollo'
import { createTask } from '../../support/gql'
import { DocumentNode } from 'graphql'

describe('Task deletion Task via API - mutation.admin.serverAvailabilityManager.createTask', () => {
    let GQL_DELETE_TASK: DocumentNode

    before('load graphql file and create test dataset', () => {
        GQL_DELETE_TASK = require(`graphql-tag/loader!../../fixtures/deleteTask.graphql`)
    })

    it('Delete task success path', () => {
        createTask('service1', 'name1', apollo())
        cy.task('apolloNode', {
            baseUrl: Cypress.config().baseUrl,
            authMethod: { username: Cypress.env('JAHIA_USERNAME'), password: Cypress.env('JAHIA_PASSWORD') },
            mode: 'mutate',
            variables: {
                service: 'service1',
                name: 'name1',
            },
            query: GQL_DELETE_TASK,
        }).then(async (response: any) => {
            cy.log(JSON.stringify(response))
            expect(response.data.admin.serverAvailabilityManager.deleteTask).to.be.true
        })
    })
    it('Should fail deleting task with wrong name and service', () => {
        createTask('service1', 'name1', apollo())
        cy.task('apolloNode', {
            baseUrl: Cypress.config().baseUrl,
            authMethod: { username: Cypress.env('JAHIA_USERNAME'), password: Cypress.env('JAHIA_PASSWORD') },
            mode: 'mutate',
            variables: {
                service: null,
                name: null,
            },
            query: GQL_DELETE_TASK,
        }).then(async (response: any) => {
            cy.log(JSON.stringify(response))
            expect(response.graphQLErrors[0].message).to.contain('Internal Server Error(s) while executing query')
        })
    })
})
