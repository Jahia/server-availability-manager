// Load type definitions that come with Cypress module
/// <reference types="cypress" />

// eslint-disable-next-line @typescript-eslint/no-namespace
declare namespace Cypress {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    interface Chainable<Subject> {
        /**
         * Custom command to navigate to url with default authentication
         * @example cy.goTo('/start')
         */
        goTo(value: string): Chainable<Element>

        apolloQuery(apollo: any, options: any): Chainable<any>

        apolloMutate(apollo: any, options: any): Chainable<any>
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

Cypress.Commands.add('apolloQuery', function (apollo: any, options: any) {
    cy.log('GQL Query', options.query.loc.source.body)
    cy.wrap({}, { log: false })
        .then(() => apollo.query(options))
        .then(async (result) => {
            cy.log('Result', JSON.stringify(result))
            return result
        })
})

Cypress.Commands.add('apolloMutate', function (apollo: any, options: any) {
    cy.log('GQL Mutation', options.mutation.loc.source.body)
    cy.wrap({}, { log: false })
        .then(() => apollo.mutate(options))
        .then(async (result) => {
            cy.log('Result', JSON.stringify(result))
            return result
        })
})
