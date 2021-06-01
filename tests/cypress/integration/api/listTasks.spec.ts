import { apollo } from '../../support/apollo'
import { createTask, deleteTask } from '../../support/gql'
import { DocumentNode } from 'graphql'

describe('List Tasks via API - mutation.admin.serverAvailabilityManager.listTasks', () => {
    let GQL_LIST_TASKS: DocumentNode

    before('load graphql file and create test dataset', () => {
        GQL_LIST_TASKS = require(`graphql-tag/loader!../../fixtures/listTasks.graphql`)
    })

    it('Get List of tasks', () => {
        createTask('service1', 'name1', apollo())
        createTask('service1', 'name2', apollo())
        createTask('service2', 'name1', apollo())
        createTask('service2', 'name1', apollo())
        //
        cy.task('apolloNode', {
            baseUrl: Cypress.config().baseUrl,
            authMethod: { username: 'root', password: 'root1234' },
            query: GQL_LIST_TASKS,
        }).then(async (response: any) => {
            cy.log(JSON.stringify(response))
            expect(response.data.admin.serverAvailabilityManager.tasks.length).to.equal(3)
            deleteTask('service1', 'name1', apollo())
            deleteTask('service1', 'name2', apollo())
            deleteTask('service2', 'name1', apollo())
            deleteTask('service2', 'name1', apollo())
        })
    })
    it('Get List of tasks empty list', () => {
        cy.task('apolloNode', {
            baseUrl: Cypress.config().baseUrl,
            authMethod: { username: 'root', password: 'root1234' },
            query: GQL_LIST_TASKS,
        }).then(async (response: any) => {
            cy.log(JSON.stringify(response))
            expect(response.data.admin.serverAvailabilityManager.tasks.length).to.equal(0)
        })
    })
})
