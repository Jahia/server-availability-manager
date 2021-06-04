/* eslint-disable @typescript-eslint/no-explicit-any */
import { createTask, deleteTask } from '../../support/gql'
import { DocumentNode } from 'graphql'

describe('Task deletion Task via API - mutation.admin.serverAvailabilityManager.createTask', () => {
    let GQL_DELETE_TASK: DocumentNode

    before('load graphql file and create test dataset', () => {
        GQL_DELETE_TASK = require(`graphql-tag/loader!../../fixtures/deleteTask.graphql`)
    })

    it('Delete task success path', () => {
        createTask('service1', 'name1')
        deleteTask('service1', 'name1').its('data.admin.serverAvailabilityManager.deleteTask').should('eq', true)
    })

    it('Should fail deleting task with wrong name and service', () => {
        createTask('service1', 'name1')
        deleteTask(null, null)
            .its('errors.0.message')
            .should('contain', 'Internal Server Error(s) while executing query')
    })
})
