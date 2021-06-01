import { apollo } from '../../support/apollo'
import { createTask, deleteTask } from '../../support/gql'

describe('Task deletion Task via API - mutation.admin.serverAvailabilityManager.createTask', () => {
    it('Delete task by providing name', async function () {
        await createTask('service1', 'name1', apollo())

        const response = await deleteTask('service1', 'name1', apollo())
        cy.log(JSON.stringify(response))
        expect(response.data.admin.serverAvailabilityManager.deleteTask).to.be.true
        expect(response.errors).to.be.undefined
    })
    it('Should fail deleting task with wrong name and service', async function () {
        await createTask('service1', 'name1', apollo())
        try {
            await deleteTask(null, null, apollo())
        } catch (err) {
            cy.log(JSON.stringify(err))
            expect(err.graphQLErrors[0].message).to.contain('Internal Server Error(s) while executing query')
        }
        //Deleting task later
        await deleteTask('service1', 'name1', apollo())
    })
})
