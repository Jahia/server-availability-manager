import { DocumentNode } from 'graphql'
import { createTask, deleteTask } from '../../support/gql'
import { apollo } from '../../support/apollo'

describe('Shutdown via API - mutation.admin.serverAvailabilityManager.shutdown', () => {
    let GQL_SHUTDOWN: DocumentNode

    before('load graphql file and create test dataset', () => {
        GQL_SHUTDOWN = require(`graphql-tag/loader!../../fixtures/shutdown.graphql`)
    })

    it('Shutdown with no tasks running (dryRun)', function () {
        cy.apolloMutate(apollo(), {
            variables: {
                dryRun: true,
            },
            mutation: GQL_SHUTDOWN,
        })
            .its('data.admin.serverAvailabilityManager.shutdown')
            .should('eq', true)
    })

    it('Shutdown impossible with tasks running (dryRun) - should exhaust default timeout (25s)', function () {
        createTask('service1', 'name1')
        const startShutdown = new Date().getTime()
        cy.apolloMutate(apollo(), {
            variables: {
                dryRun: true,
            },
            mutation: GQL_SHUTDOWN,
        }).should((response) => {
            cy.log('Requested shutdown')
            expect(response.data.admin.serverAvailabilityManager.shutdown).to.be.false
            const completeShutdown = new Date().getTime()
            const executionTime = completeShutdown - startShutdown
            cy.log(`Execution time: ${executionTime}`)
            expect(executionTime).to.be.greaterThan(25000)
            expect(executionTime).not.to.be.greaterThan(28000)
        })
        deleteTask('service1', 'name1')
    })

    it('Shutdown impossible with tasks running (dryRun) - shorter timeout (2s)', function () {
        createTask('service1', 'name1')
        const startShutdown = new Date().getTime()
        cy.apolloMutate(apollo(), {
            variables: {
                dryRun: true,
                timeout: 2,
            },
            mutation: GQL_SHUTDOWN,
        }).should((response) => {
            cy.log('Requested shutdown')
            expect(response.data.admin.serverAvailabilityManager.shutdown).to.be.false
            const completeShutdown = new Date().getTime()
            const executionTime = completeShutdown - startShutdown
            cy.log(`Execution time: ${executionTime}`)
            expect(executionTime).to.be.greaterThan(2000)
            expect(executionTime).not.to.be.greaterThan(5000)
        })
        deleteTask('service1', 'name1')
    })

    it('Force shutdown without tasks running (dryRun)', function () {
        const startShutdown = new Date().getTime()
        cy.apolloMutate(apollo(), {
            variables: {
                dryRun: true,
                force: true,
            },
            mutation: GQL_SHUTDOWN,
        }).should((response) => {
            expect(response.data.admin.serverAvailabilityManager.shutdown).to.be.true
            const completeShutdown = new Date().getTime()
            const executionTime = completeShutdown - startShutdown
            cy.log(`Execution time: ${executionTime}`)
            expect(executionTime).not.to.be.greaterThan(2000)
        })
    })

    it('Force shutdown with tasks running (dryRun)', function () {
        createTask('service1', 'name1')
        const startShutdown = new Date().getTime()
        cy.apolloMutate(apollo(), {
            variables: {
                dryRun: true,
                force: true,
            },
            mutation: GQL_SHUTDOWN,
        }).should((response) => {
            cy.log('Requested shutdown')
            expect(response.data.admin.serverAvailabilityManager.shutdown).to.be.true
            const completeShutdown = new Date().getTime()
            const executionTime = completeShutdown - startShutdown
            cy.log(`Execution time: ${executionTime}`)
            expect(executionTime).not.to.be.greaterThan(2000)
        })
        deleteTask('service1', 'name1')
    })

    it('Should fail Shutdown wrong timeout format', function () {
        cy.apolloMutate(apollo(), {
            variables: {
                timeout: 'ABC',
            },
            mutation: GQL_SHUTDOWN,
            errorPolicy: 'all',
        })
            .its('errors.0.message')
            .should('contain', 'Internal Server Error(s) while executing query')
    })
})
