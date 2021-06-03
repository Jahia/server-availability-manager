/* eslint-disable @typescript-eslint/no-explicit-any */
import { DocumentNode } from 'graphql'
import { createTask, deleteTask } from '../../support/gql'
import { apollo } from '../../support/apollo'

describe('Shutdown via API - mutation.admin.serverAvailabilityManager.shutdown', () => {
    let GQL_SHUTDOWN: DocumentNode

    before('load graphql file and create test dataset', () => {
        GQL_SHUTDOWN = require(`graphql-tag/loader!../../fixtures/shutdown.graphql`)
    })

    it('Shutdown with no tasks running (dryRun)', function () {
        cy.task('apolloNode', {
            baseUrl: Cypress.config().baseUrl,
            authMethod: { username: Cypress.env('JAHIA_USERNAME'), password: Cypress.env('JAHIA_PASSWORD') },
            mode: 'mutate',
            variables: {
                dryRun: true,
            },
            query: GQL_SHUTDOWN,
        }).then((response: any) => {
            cy.log(JSON.stringify(response))
            expect(response.data.admin.serverAvailabilityManager.shutdown).to.be.true
        })
    })

    it('Shutdown impossible with tasks running (dryRun) - should exhaust default timeout (25s)', function () {
        cy.wrap(createTask('service1', 'name1', apollo()))
        const startShutdown = new Date().getTime()
        cy.task('apolloNode', {
            baseUrl: Cypress.config().baseUrl,
            authMethod: { username: Cypress.env('JAHIA_USERNAME'), password: Cypress.env('JAHIA_PASSWORD') },
            mode: 'mutate',
            variables: {
                dryRun: true,
            },
            query: GQL_SHUTDOWN,
        }).then((response: any) => {
            cy.log(JSON.stringify(response))
            cy.log('Requested shutdown')
            expect(response.data.admin.serverAvailabilityManager.shutdown).to.be.false
            const completeShutdown = new Date().getTime()
            const executionTime = completeShutdown - startShutdown
            cy.log(`Execution time: ${executionTime}`)
            expect(executionTime).to.be.greaterThan(25000)
            expect(executionTime).not.to.be.greaterThan(28000)
        })
        cy.wrap(deleteTask('service1', 'name1', apollo()))
    })

    it('Shutdown impossible with tasks running (dryRun) - shorter timeout (2s)', function () {
        cy.wrap(createTask('service1', 'name1', apollo()))
        const startShutdown = new Date().getTime()
        cy.task('apolloNode', {
            baseUrl: Cypress.config().baseUrl,
            authMethod: { username: Cypress.env('JAHIA_USERNAME'), password: Cypress.env('JAHIA_PASSWORD') },
            mode: 'mutate',
            variables: {
                dryRun: true,
                timeout: 2,
            },
            query: GQL_SHUTDOWN,
        }).then((response: any) => {
            cy.log(JSON.stringify(response))
            cy.log('Requested shutdown')
            expect(response.data.admin.serverAvailabilityManager.shutdown).to.be.false
            const completeShutdown = new Date().getTime()
            const executionTime = completeShutdown - startShutdown
            cy.log(`Execution time: ${executionTime}`)
            expect(executionTime).to.be.greaterThan(2000)
            expect(executionTime).not.to.be.greaterThan(5000)
        })
        cy.wrap(deleteTask('service1', 'name1', apollo()))
    })

    it('Force shutdown without tasks running (dryRun)', function () {
        const startShutdown = new Date().getTime()
        cy.task('apolloNode', {
            baseUrl: Cypress.config().baseUrl,
            authMethod: { username: Cypress.env('JAHIA_USERNAME'), password: Cypress.env('JAHIA_PASSWORD') },
            mode: 'mutate',
            variables: {
                dryRun: true,
                force: true,
            },
            query: GQL_SHUTDOWN,
        }).then((response: any) => {
            cy.log(JSON.stringify(response))
            expect(response.data.admin.serverAvailabilityManager.shutdown).to.be.true
            const completeShutdown = new Date().getTime()
            const executionTime = completeShutdown - startShutdown
            cy.log(`Execution time: ${executionTime}`)
            expect(executionTime).not.to.be.greaterThan(2000)
        })
    })

    it('Force shutdown with tasks running (dryRun)', function () {
        cy.wrap(createTask('service1', 'name1', apollo()))
        const startShutdown = new Date().getTime()
        cy.task('apolloNode', {
            baseUrl: Cypress.config().baseUrl,
            authMethod: { username: Cypress.env('JAHIA_USERNAME'), password: Cypress.env('JAHIA_PASSWORD') },
            mode: 'mutate',
            variables: {
                dryRun: true,
                force: true,
            },
            query: GQL_SHUTDOWN,
        }).then((response: any) => {
            cy.log(JSON.stringify(response))
            cy.log('Requested shutdown')
            expect(response.data.admin.serverAvailabilityManager.shutdown).to.be.true
            const completeShutdown = new Date().getTime()
            const executionTime = completeShutdown - startShutdown
            cy.log(`Execution time: ${executionTime}`)
            expect(executionTime).not.to.be.greaterThan(2000)
        })
        cy.wrap(deleteTask('service1', 'name1', apollo()))
    })

    it('Should fail Shutdown wrong timeout format', function () {
        cy.task('apolloNode', {
            baseUrl: Cypress.config().baseUrl,
            authMethod: { username: Cypress.env('JAHIA_USERNAME'), password: Cypress.env('JAHIA_PASSWORD') },
            mode: 'mutate',
            variables: {
                timeout: 'ABC',
            },
            query: GQL_SHUTDOWN,
        }).then((response: any) => {
            cy.log(JSON.stringify(response))
            expect(response.graphQLErrors[0].message).to.contain('Internal Server Error(s) while executing query')
        })
    })
})
