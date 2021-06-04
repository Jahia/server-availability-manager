import { createTask, deleteTask } from '../../support/gql'

describe('Task deletion Task via API - mutation.admin.serverAvailabilityManager.createTask', () => {
    it('Delete task success path', () => {
        createTask('service1', 'name1')
        deleteTask('service1', 'name1').its('data.admin.serverAvailabilityManager.deleteTask').should('eq', true)
    })
    it('Should fail deleting task with empty service name', function () {
        deleteTask(null, 'name1').its('errors.0.message').should('contains', 'Service name not provided')
    })
    it('Should fail deleting task with empty name', function () {
        deleteTask('service1', null).its('errors.0.message').should('contains', 'Task name not provided')
    })
    it('Should fail deleting non existent task', function () {
        deleteTask('anyService', 'anyName').its('data.admin.serverAvailabilityManager.deleteTask').should('eq', false)
    })
})
