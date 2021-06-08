import { createTask, deleteTask } from '../../support/gql'

describe('Task deletion Task via API - mutation.admin.serverAvailabilityManager.createTask', () => {
    it('Delete task success path', () => {
        createTask('service1', 'name1')
        deleteTask('service1', 'name1').its('data.admin.serverAvailabilityManager.deleteTask').should('eq', true)
    })
    it('Should fail deleting non existent task', function () {
        deleteTask('anyService', 'anyName').its('data.admin.serverAvailabilityManager.deleteTask').should('eq', false)
    })
})
