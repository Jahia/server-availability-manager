import { createTask, deleteTask } from '../../support/gql'
import { DocumentNode } from 'graphql'
import { apollo } from '../../support/apollo'

describe('List Tasks via API - mutation.admin.serverAvailabilityManager.listTasks', () => {
    let GQL_LIST_TASKS: DocumentNode

    before('load graphql file and create test dataset', () => {
        GQL_LIST_TASKS = require(`graphql-tag/loader!../../fixtures/listTasks.graphql`)
    })

    it('Get List of tasks', () => {
        createTask('service1', 'name1')
        createTask('service1', 'name2')
        createTask('service2', 'name1')
        cy.apolloQuery(apollo(), { query: GQL_LIST_TASKS })
            .its('data.admin.serverAvailabilityManager.tasks.length')
            .should('equals', 3)
        deleteTask('service1', 'name1')
        deleteTask('service1', 'name2')
        deleteTask('service2', 'name1')
    })

    it('Get List of tasks empty list', () => {
        cy.apolloQuery(apollo(), { query: GQL_LIST_TASKS })
            .its('data.admin.serverAvailabilityManager.tasks.length')
            .should('equals', 0)
    })
})
