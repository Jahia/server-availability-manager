/* eslint-disable @typescript-eslint/no-explicit-any */
import { ApolloClient, NormalizedCacheObject } from '@apollo/client/core'
import Chainable = Cypress.Chainable
import { apollo } from './apollo'

export const createTask = (
    taskService: string,
    taskName: string,
    apolloClient: ApolloClient<NormalizedCacheObject> = apollo(),
): Chainable<any> => {
    return cy.apolloMutate(apolloClient, {
        variables: {
            service: taskService,
            name: taskName,
        },
        errorPolicy: 'all',
        mutation: require(`graphql-tag/loader!../fixtures/createTask.graphql`),
    })
}

export const deleteTask = (
    taskService: string,
    taskName: string,
    apolloClient: ApolloClient<NormalizedCacheObject> = apollo(),
): Chainable<any> => {
    return cy.apolloMutate(apolloClient, {
        variables: {
            service: taskService,
            name: taskName,
        },
        errorPolicy: 'all',
        mutation: require(`graphql-tag/loader!../fixtures/deleteTask.graphql`),
    })
}

export const healthCheck = (
    severity: string,
    apolloClient: ApolloClient<NormalizedCacheObject> = apollo(),
): Chainable<any> => {
    return cy
        .apolloQuery(apolloClient, {
            query: require(`graphql-tag/loader!../fixtures/healthcheck.graphql`),
            variables: {
                severity,
            },
        })
        .then((response: any) => {
            return response.data.admin.serverAvailabilityManager.healthCheck
        })
}

export const load = (
    interval: string,
    apolloClient: ApolloClient<NormalizedCacheObject> = apollo(),
): Chainable<any> => {
    return cy
        .apolloQuery(apolloClient, {
            query: require(`graphql-tag/loader!../fixtures/load.graphql`),
            variables: {
                interval,
            },
        })
        .then((response: any) => {
            console.log(response)
            return response.data.admin.serverAvailabilityManager.load
        })
}
