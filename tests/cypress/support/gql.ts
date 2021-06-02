/* eslint-disable @typescript-eslint/no-explicit-any */
import { ApolloClient, NormalizedCacheObject } from '@apollo/client/core'

export function createTask(
    service: string,
    name: string,
    apolloClient: ApolloClient<NormalizedCacheObject>,
): Promise<any> {
    return apolloClient.mutate({
        mutation: require(`graphql-tag/loader!../fixtures/createTask.graphql`),
        variables: {
            service,
            name,
        },
    })
}

export function deleteTask(
    service: string,
    name: string,
    apolloClient: ApolloClient<NormalizedCacheObject>,
): Promise<any> {
    return apolloClient.mutate({
        mutation: require(`graphql-tag/loader!../fixtures/deleteTask.graphql`),
        variables: {
            service,
            name,
        },
    })
}
