/* eslint-disable @typescript-eslint/no-explicit-any */
import { createTask, deleteTask } from '../../support/gql'
import { DocumentNode } from 'graphql'

describe('List Tasks via API - mutation.admin.serverAvailabilityManager.listTasks', () => {
    let GQL_LIST_TASKS: DocumentNode

    before('load graphql file and create test dataset', () => {
        GQL_LIST_TASKS = require(`graphql-tag/loader!../../fixtures/listTasks.graphql`)
    })

    it('Get List of tasks', () => {
        createTask('service1', 'name1')
        createTask('service1', 'name2')
        createTask('service2', 'name1')
        createTask('service2', 'name1')
        //
        cy.task('apolloNode', {
            baseUrl: Cypress.config().baseUrl,
            authMethod: { username: 'root', password: Cypress.env('SUPER_USER_PASSWORD') },
            query: GQL_LIST_TASKS,
        }).then((response: any) => {
            cy.log(JSON.stringify(response))
            expect(response.data.admin.serverAvailabilityManager.tasks.length).to.equal(3)
        })
        deleteTask('service1', 'name1')
        deleteTask('service1', 'name2')
        deleteTask('service2', 'name1')
        deleteTask('service2', 'name1')
    })

    it('Get List of tasks empty list', () => {
        cy.task('apolloNode', {
            baseUrl: Cypress.config().baseUrl,
            authMethod: { username: 'root', password: Cypress.env('SUPER_USER_PASSWORD') },
            query: GQL_LIST_TASKS,
        }).then((response: any) => {
            cy.log(JSON.stringify(response))
            expect(response.data.admin.serverAvailabilityManager.tasks.length).to.equal(0)
        })
    })
})
