// Load type definitions that come with Cypress module
/// <reference types="cypress" />

// eslint-disable-next-line @typescript-eslint/no-namespace
// import {apollo} from "./apollo";
// import {QueryOptions} from "@apollo/client/core";

declare namespace Cypress {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    interface Chainable<Subject> {
        /**
         * Custom command to navigate to url with default authentication
         * @example cy.goTo('/start')
         */
        goTo(value: string): Chainable<Element>;

        apolloQuery(apollo:any, options: any): Chainable<object>;

        apolloMutate(apollo:any, options: any): Chainable<object>;
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
    cy.wrap({}).then(() => apollo.query(options))
})

Cypress.Commands.add('apolloMutate', function (apollo: any, options: any) {
    cy.wrap({}).then(() => apollo.mutate(options))
})
