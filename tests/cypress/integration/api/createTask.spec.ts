import { createTask, deleteTask } from '../../support/gql'
import {ApolloClient, NormalizedCacheObject} from "@apollo/client/core";

describe('Task creation via API - mutation.admin.jahia.createTask', () => {
    it('Create task by providing service, name', () => {
        createTask('service1', 'name1').its('data.admin.jahia.createTask').should('eq', true)
        deleteTask('service1', 'name1')
    })

    it('Should fail creating task with big service name', function () {
        createTask('12345678901234567890123456789012345678901234567890AA', 'name1')
            .its('errors.0.message')
            .should('contains', 'Service is not a alphanumerical with a limited length of 50 characters')
    })
    it('Should fail creating task with guest user', () => {
        createTask('service1', 'name1', { username: 'guest', password: null })
            .its('errors.0.message')
            .should('contains', 'Permission denied')
    })
})
