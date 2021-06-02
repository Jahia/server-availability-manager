/* eslint-disable @typescript-eslint/no-explicit-any */
import { DocumentNode } from 'graphql'
import { deleteTask } from '../../support/gql'
import { apollo } from '../../support/apollo'

describe('Task creation via API - mutation.admin.serverAvailabilityManager.createTask', () => {
    let GQL_CREATE_TASK: DocumentNode

    before('load graphql file and create test dataset', () => {
        GQL_CREATE_TASK = require(`graphql-tag/loader!../../fixtures/createTask.graphql`)
    })

    it('Create task by providing service, name', () => {
        cy.task('apolloNode', {
            baseUrl: Cypress.config().baseUrl,
            authMethod: { username: Cypress.env('JAHIA_USERNAME'), password: Cypress.env('JAHIA_PASSWORD') },
            mode: 'mutate',
            variables: {
                service: 'service1',
                name: 'name1',
            },
            query: GQL_CREATE_TASK,
        }).then(async (response: any) => {
            cy.log(JSON.stringify(response))
            expect(response.data.admin.serverAvailabilityManager.createTask).to.be.true
            deleteTask('service1', 'name1', apollo())
        })
    })
    it('Should fail creating task with big service name', function () {
        cy.task('apolloNode', {
            baseUrl: Cypress.config().baseUrl,
            authMethod: { username: Cypress.env('JAHIA_USERNAME'), password: Cypress.env('JAHIA_PASSWORD') },
            mode: 'mutate',
            variables: {
                service: '12345678901234567890123456789012345678901234567890AA',
                name: 'name1',
            },
            query: GQL_CREATE_TASK,
        }).then(async (response: any) => {
            cy.log(JSON.stringify(response))
            expect(response.graphQLErrors[0].message).to.contains(
                'Service is not a alphanumerical with a limited length of 50 characters',
            )
        })
    })
    it('Should fail creating task with empty service name', function () {
        cy.task('apolloNode', {
            baseUrl: Cypress.config().baseUrl,
            authMethod: { username: Cypress.env('JAHIA_USERNAME'), password: Cypress.env('JAHIA_PASSWORD') },
            mode: 'mutate',
            variables: {
                service: null,
                name: 'name1',
            },
            query: GQL_CREATE_TASK,
        }).then(async (response: any) => {
            cy.log(JSON.stringify(response))
            expect(response.graphQLErrors[0].message).to.contain('Internal Server Error(s) while executing query')
        })
    })
    it('Should fail creating task with empty name', function () {
        cy.task('apolloNode', {
            baseUrl: Cypress.config().baseUrl,
            authMethod: { username: Cypress.env('JAHIA_USERNAME'), password: Cypress.env('JAHIA_PASSWORD') },
            mode: 'mutate',
            variables: {
                service: 'service1',
                name: null,
            },
            query: GQL_CREATE_TASK,
        }).then(async (response: any) => {
            cy.log(JSON.stringify(response))
            expect(response.graphQLErrors[0].message).to.contain('Internal Server Error(s) while executing query')
        })
    })
    //TODO Fix - Something wrong with users (looks like it is not taking username in consideration)
    // it('Should fail creating task with guess user', function () {
    //     cy.task('apolloNode', {
    //         baseUrl: Cypress.config().baseUrl,
    //         authMethod: { username: 'guest', password: null },
    //         mode: 'mutate',
    //         variables: {
    //             service: 'service1',
    //             name: 'name1',
    //         },
    //         query: GQL_CREATE_TASK,
    //     }).then(async (response: any) => {
    //         cy.log(JSON.stringify(response))
    //         expect(response.graphQLErrors[0].message).to.contain('Internal Server Error(s) while executing query')
    //     })
    // })
})
