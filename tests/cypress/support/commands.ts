/* eslint-disable @typescript-eslint/no-explicit-any, @typescript-eslint/no-namespace */

// Load type definitions that come with Cypress module
/// <reference types="cypress" />

import { MutationOptions, QueryOptions } from '@apollo/client'
import { ApolloClient } from '@apollo/client/core'

declare global {
    namespace Cypress {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        interface Chainable<Subject> {
            /**
             * Custom command to navigate to url with default authentication
             * @example cy.goTo('/start')
             */
            goTo(value: string): Chainable<Element>

            apolloQuery(apollo: ApolloClient<any>, options: QueryOptions): Chainable<any>

            apolloMutate(apollo: ApolloClient<any>, options: MutationOptions): Chainable<any>

            runProvisioningScript(body: string, type?: string): Chainable<Response>

            downloadAndInstallModuleFromStore(module: string, version: string): Chainable<Response>

            uninstallModule(module: string, version: string): Chainable<Response>

            runGroovyScript(script: string): Chainable<Response>
        }
    }
}

Cypress.Commands.add('goTo', function (url: string) {
    cy.visit(url, {
        auth: {
            username: Cypress.env('root'),
            password: Cypress.env('SUPER_USER_PASSWORD'),
        },
    })
})

Cypress.Commands.add('apolloQuery', function (apollo: ApolloClient<any>, options: QueryOptions) {
    cy.log('GQL Query', options.query.loc.source.body)
    cy.wrap({}, { log: false })
        .then(() => apollo.query(options))
        .then(async (result) => {
            cy.log('Result', JSON.stringify(result))
            return result
        })
})

Cypress.Commands.add('apolloMutate', function (apollo: ApolloClient<any>, options: MutationOptions) {
    cy.log('GQL Mutation', options.mutation.loc.source.body)
    cy.wrap({}, { log: false })
        .then(() => apollo.mutate(options))
        .then(async (result) => {
            cy.log('Result', JSON.stringify(result))
            return result
        })
})

Cypress.Commands.add('runProvisioningScript', function (body: string, type = 'application/json') {
    cy.request({
        url: `${Cypress.config().baseUrl}/modules/api/provisioning`,
        method: 'POST',
        auth: {
            user: 'root',
            pass: Cypress.env('SUPER_USER_PASSWORD'),
            sendImmediately: true,
        },
        headers: {
            'content-type': type,
        },
        body,
    })
    // eslint-disable-next-line cypress/no-unnecessary-waiting
    cy.wait(100)
})

Cypress.Commands.add('downloadAndInstallModuleFromStore', function (module: string, version: string) {
    cy.task('installModule', { name: module, version: version })
    // eslint-disable-next-line cypress/no-unnecessary-waiting
    cy.wait(100)
})

Cypress.Commands.add('uninstallModule', function (module: string, version: string) {
    cy.task('uninstallModule', { name: module, version: version, key: `${module}\/${version}` })
    // eslint-disable-next-line cypress/no-unnecessary-waiting
    cy.wait(1000)
})

Cypress.Commands.add('runGroovyScript', function (script: string) {
    cy.task('runGroovyScript', script)
    // eslint-disable-next-line cypress/no-unnecessary-waiting
    cy.wait(100)
})
