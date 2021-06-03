/* eslint-disable @typescript-eslint/no-explicit-any */
import {ApolloClient, NormalizedCacheObject} from "@apollo/client/core";
import Chainable = Cypress.Chainable;

export const createTask = (taskService: string, taskName: string): void => {
    cy.task('apolloNode', {
        baseUrl: Cypress.config().baseUrl,
        authMethod: { username: 'root', password: Cypress.env('SUPER_USER_PASSWORD') },
        mode: 'mutate',
        variables: {
            service: taskService,
            name: taskName,
        },
        query: require(`graphql-tag/loader!../fixtures/createTask.graphql`),
    }).then((response: any) => {
        cy.log(JSON.stringify(response))
        cy.log('Task created')
        expect(response.data.admin.serverAvailabilityManager.createTask).to.be.true
    })
    return
}

export const deleteTask = (taskService: string, taskName: string): void => {
    cy.task('apolloNode', {
        baseUrl: Cypress.config().baseUrl,
        authMethod: { username: 'root', password: Cypress.env('SUPER_USER_PASSWORD') },
        mode: 'mutate',
        variables: {
            service: taskService,
            name: taskName,
        },
        query: require(`graphql-tag/loader!../fixtures/deleteTask.graphql`),
    }).then((response: any) => {
        cy.log(JSON.stringify(response))
        cy.log('Deleted task')
        expect(response.data.admin.serverAvailabilityManager.deleteTask).to.be.true
    })
    return
}

export function healthCheck(
    severity: string,
    apolloClient: ApolloClient<NormalizedCacheObject>,
): Chainable<any> {
    return cy.apolloQuery(apolloClient, {
        query: require(`graphql-tag/loader!../fixtures/healthcheck.graphql`),
        variables: {
            severity
        },
    })
}
