import { createTask, deleteTask } from '../../support/gql'
import {apollo} from "../../support/apollo";

describe('Task deletion Task via API - mutation.admin.jahia.createTask', () => {
    it('Delete task success path', () => {
        createTask('service1', 'name1')
        deleteTask('service1', 'name1').its('data.admin.jahia.deleteTask').should('eq', true)
    })
    it('Should fail deleting non existent task', function () {
        deleteTask('anyService', 'anyName').its('data.admin.jahia.deleteTask').should('eq', false)
    })
    it('Should fail deleting task with guest user', () => {
        deleteTask('service1', 'name1', apollo({ username: 'guest', password: null }))
            .its('errors.0.message')
            .should('contains', 'Permission denied')
    })
})
