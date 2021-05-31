import { ApolloClient, HttpLink, InMemoryCache, NormalizedCacheObject } from '@apollo/client/core'

interface authMethod {
    token?: string
    username?: string
    password?: string
    jsessionid?: string
}

export const apollo = (authMethod?: authMethod): ApolloClient<NormalizedCacheObject> => {
    const headers: { authorization?: string } = {}
    if (authMethod === undefined) {
        headers.authorization = `Basic ${btoa(Cypress.env('JAHIA_USERNAME') + ':' + Cypress.env('JAHIA_PASSWORD'))}`
    } else if (authMethod.token !== undefined) {
        headers.authorization = `APIToken ${authMethod.token}`
    } else if (authMethod.username !== undefined && authMethod.password !== undefined) {
        headers.authorization = `Basic ${btoa(authMethod.username + ':' + authMethod.password)}`
    }
    // Otherwise, no headers are sent and user is considered guest (i.e. apolloClient({}))
    cy.log(JSON.stringify(headers))

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
