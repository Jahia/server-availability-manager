/* eslint-disable @typescript-eslint/no-explicit-any */
import { DocumentNode } from 'graphql'
import { createTask, deleteTask } from '../../support/gql'
import { apollo } from '../../support/apollo'

describe('Task creation via API - mutation.admin.serverAvailabilityManager.createTask', () => {
    it('Create task by providing service, name', () => {
        createTask('service1', 'name1').its('data.admin.serverAvailabilityManager.createTask').should('eq', true)
        deleteTask('service1', 'name1')
    })

    it('Should fail creating task with big service name', function () {
        createTask('12345678901234567890123456789012345678901234567890AA', 'name1')
            .its('errors.0.message')
            .should('contains', 'Service is not a alphanumerical with a limited length of 50 characters')
    })

    it('Should fail creating task with empty service name', function () {
        createTask(null, 'name1')
            .its('errors.0.message')
            .should('contains', 'Internal Server Error(s) while executing query')
    })

    it('Should fail creating task with empty name', function () {
        createTask('service1', null)
            .its('errors.0.message')
            .should('contains', 'Internal Server Error(s) while executing query')
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
    //     }).then((response: any) => {
    //         cy.log(JSON.stringify(response))
    //         expect(response.graphQLErrors[0].message).to.contain('Internal Server Error(s) while executing query')
    //     })
    // })
})
