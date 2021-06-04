import { createTask, deleteTask } from '../../support/gql'

describe('Task deletion Task via API - mutation.admin.serverAvailabilityManager.createTask', () => {
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
