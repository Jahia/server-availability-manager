import { ApolloClient, ApolloQueryResult, QueryOptions } from '@apollo/client/core';
declare global {
    namespace Cypress {
        interface Chainable<Subject> {
            apolloQuery(options: QueryOptions): Chainable<ApolloQueryResult<any>>;
        }
    }
}
export declare const apolloQuery: (apollo: ApolloClient<any>, options: QueryOptions & Cypress.Loggable) => void;
