"use strict";
/* eslint-disable @typescript-eslint/no-explicit-any, @typescript-eslint/no-namespace */
Object.defineProperty(exports, "__esModule", { value: true });
exports.apolloClient = void 0;
const core_1 = require("@apollo/client/core");
const apolloClient = function (authMethod, options = {
    log: true,
    setCurrentApolloClient: true
}) {
    const headers = {};
    if (authMethod === undefined) {
        headers.authorization = `Basic ${btoa('root:' + Cypress.env('SUPER_USER_PASSWORD'))}`;
    }
    else if (authMethod.token !== undefined) {
        headers.authorization = `APIToken ${authMethod.token}`;
    }
    else if (authMethod.username !== undefined && authMethod.password !== undefined) {
        headers.authorization = `Basic ${btoa(authMethod.username + ':' + authMethod.password)}`;
    }
    const client = new core_1.ApolloClient({
        link: new core_1.HttpLink({
            uri: `${Cypress.config().baseUrl}/modules/graphql`,
            headers,
        }),
        cache: new core_1.InMemoryCache(),
        defaultOptions: {
            query: {
                fetchPolicy: 'no-cache',
            },
        },
    });
    if (options.log) {
        Cypress.log({
            name: 'apolloClient',
            displayName: 'apClient',
            message: `Create new apollo client`,
            consoleProps: () => {
                return {
                    Auth: authMethod,
                    Yielded: client
                };
            },
        });
    }
    if (options.setCurrentApolloClient) {
        cy.wrap(client, { log: false }).as('currentApolloClient');
    }
    else {
        cy.wrap(client, { log: false });
    }
};
exports.apolloClient = apolloClient;
