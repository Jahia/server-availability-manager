import { ApolloClient, HttpLink, InMemoryCache, NormalizedCacheObject } from '@apollo/client/core'

interface AuthMethod {
    token?: string
    username?: string
    password?: string
    jsessionid?: string
}

export const apollo = (authMethod?: AuthMethod): ApolloClient<NormalizedCacheObject> => {
    const headers: { authorization?: string } = {}
    if (authMethod === undefined) {
        headers.authorization = `Basic ${btoa('root:' + Cypress.env('SUPER_USER_PASSWORD'))}`
    } else if (authMethod.token !== undefined) {
        headers.authorization = `APIToken ${authMethod.token}`
    } else if (authMethod.username !== undefined && authMethod.password !== undefined) {
        headers.authorization = `Basic ${btoa(authMethod.username + ':' + authMethod.password)}`
    }

    return new ApolloClient({
        link: new HttpLink({
            uri: `${Cypress.config().baseUrl}/modules/graphql`,
            headers,
        }),
        cache: new InMemoryCache(),
        defaultOptions: {
            query: {
                fetchPolicy: 'no-cache',
            },
        },
    })
}
