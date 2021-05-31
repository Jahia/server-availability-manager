import { apollo } from '../../support/apollo'
import { createTask, deleteTask } from '../../support/gql'

describe('Task creation via API - mutation.admin.serverAvailabilityManager.createTask', () => {
    it('Create task by providing service, name', async function () {
        const response = await createTask('service1', 'name1', apollo())
        cy.log(JSON.stringify(response))
        expect(response.data.admin.serverAvailabilityManager.createTask).to.be.true
        expect(response.errors).to.be.undefined

        //Deleting task later
        await deleteTask('name1', apollo())
    })
    it('Should fail creating task with big service name', async function () {
        try {
            await createTask('12345678901234567890123456789012345678901234567890AA', 'name1', apollo())
        } catch (err) {
            cy.log(JSON.stringify(err))
            expect(err.graphQLErrors[0].message).to.contain(
                'Service is not a alphanumerical with a limited length of 50 characters',
            )
        }
        //Deleting task later
        await deleteTask('name1', apollo())
    })
    it('Should fail creating task with empty service name', async function () {
        try {
            await createTask(null, 'name1', apollo())
        } catch (err) {
            cy.log(JSON.stringify(err))
            expect(err.graphQLErrors[0].message).to.contain('Internal Server Error(s) while executing query')
        }
        //Deleting task later
        await deleteTask('name1', apollo())
    })
})
