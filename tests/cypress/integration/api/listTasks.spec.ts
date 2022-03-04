import { createTask, deleteTask } from '../../support/gql'
import { DocumentNode } from 'graphql'

describe('List Tasks via API - mutation.admin.jahia.listTasks', () => {
    it('Get List of tasks', () => {
        createTask('service1', 'name1')
        createTask('service1', 'name2')
        createTask('service2', 'name1')
        cy.apollo({ queryFile: 'listTasks.graphql' }).its('data.admin.jahia.tasks.length').should('equals', 3)
        deleteTask('service1', 'name1')
        deleteTask('service1', 'name2')
        deleteTask('service2', 'name1')
    })

    it('Get List of tasks empty list', () => {
        cy.apollo({ queryFile: 'listTasks.graphql' }).its('data.admin.jahia.tasks.length').should('equals', 0)
    })
})
