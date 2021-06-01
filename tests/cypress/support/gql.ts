/* eslint-disable @typescript-eslint/no-explicit-any */
import { ApolloClient, NormalizedCacheObject } from '@apollo/client/core'

export async function createTask(
    service: string,
    name: string,
    apolloClient: ApolloClient<NormalizedCacheObject>,
): Promise<any> {
    return await apolloClient.mutate({
        mutation: require(`graphql-tag/loader!../fixtures/createTask.graphql`),
        variables: {
            service,
            name,
        },
    })
}

export async function deleteTask(
    service: string,
    name: string,
    apolloClient: ApolloClient<NormalizedCacheObject>,
): Promise<any> {
    return await apolloClient.mutate({
        mutation: require(`graphql-tag/loader!../fixtures/deleteTask.graphql`),
        variables: {
            service,
            name,
        },
    })
}
