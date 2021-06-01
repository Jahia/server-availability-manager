import { DocumentNode } from 'graphql'

describe('Task creation via API - mutation.admin.serverAvailabilityManager.createTask', () => {
    let GQL_CREATE_TASK: DocumentNode

    before('load graphql file and create test dataset', () => {
        GQL_CREATE_TASK = require(`graphql-tag/loader!../../fixtures/createTask.graphql`)
    })

    it('Create task by providing service, name', () => {
        cy.task('apolloNode', {
            baseUrl: Cypress.config().baseUrl,
            authMethod: { username: 'root', password: 'root1234' },
            mode: 'mutate',
            variables: {
                service: 'service1',
                name: 'name1',
            },
            query: GQL_CREATE_TASK,
        }).then(async (response: any) => {
            cy.log(JSON.stringify(response))
            expect(response.data.admin.serverAvailabilityManager.createTask).to.be.true
        })
    })
})
