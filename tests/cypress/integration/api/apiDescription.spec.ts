import gql from 'graphql-tag'
import { ApolloClient, NormalizedCacheObject } from '@apollo/client/core'

describe('Test if every type in graphQL API has description', () => {
    it('Check every input for the User Type', function () {
        const noDesc = new Set<string>()
        cy.apolloClient().then(async (client) => {
            await executeTest(client, 'ServerAvailabilityManagerQuery', noDesc)
            await executeTest(client, 'ServerAvailabilityManagerMutation', noDesc)
            expect(JSON.stringify(Array.from(noDesc))).to.equals('[]')
        })
    })
})

// Test to go down the AST of GraphQL to check for descriptions
const executeTest = async (client: ApolloClient<NormalizedCacheObject>, typeName: string, noDesc: Set<string>) => {
    const query = constructQuery(typeName)
    const response = await client.query({ query })
    const responseDataType = response.data.__type
    if (responseDataType === null || responseDataType === undefined || responseDataType.kind === 'UNION') {
        return
    }

    if (!responseDataType.description) {
        noDesc.add('type=' + responseDataType.name)
    }

    if (responseDataType.fields) {
        await asyncForEach(responseDataType.fields, async (field) => {
            if (field.args) {
                await asyncForEach(field.args, async (arg) => {
                    await fieldCheck(
                        client,
                        'type=' + responseDataType.name + '/field=' + field.name + '/arg=' + arg.name,
                        arg,
                        noDesc,
                    )
                })
            }

            await fieldCheck(client, 'type=' + responseDataType.name + '/field=' + field.name, field, noDesc)
        })
    }

    if (responseDataType.inputFields) {
        await asyncForEach(responseDataType.inputFields, async (field) => {
            if (field.args) {
                await asyncForEach(field.args, async (arg) => {
                    await fieldCheck(client, 'inputType=' + responseDataType.name + '/arg=' + arg.name, arg, noDesc)
                })
            }

            await fieldCheck(client, 'inputType=' + responseDataType.name + '/field=' + field.name, field, noDesc)
        })
    }
}

const fieldCheck = async (client, message, field, noDesc) => {
    if (field.description === null) {
        noDesc.add(message)
    }

    if (field.type.kind === 'OBJECT' || field.type.kind === 'INPUT_OBJECT') {
        await executeTest(client, field.type.name, noDesc)
    }

    if (field.type.kind === 'NON_NULL' && field.type.ofType.kind === 'LIST') {
        await executeTest(client, field.type.ofType.ofType.name, noDesc)
    }

    if (field.type.kind === 'LIST') {
        await executeTest(client, field.type.ofType.name, noDesc)
    }
}

const asyncForEach = async (array, callback) => {
    for (let index = 0; index < array.length; index++) {
        // eslint-disable-next-line no-await-in-loop
        await callback(array[index], index, array)
    }
}

const constructQuery = (typeName) => {
    return gql`query IntrospectionQuery {
        __type(name:"${typeName}") {
            kind
            name
            description
            fields {
                name
                description
                args {
                    name
                    description
                    type {
                        kind
                        name
                        description
                        ofType {
                            kind
                            name
                            description
                        }
                    }
                }
                type {
                    kind
                    name
                    description
                    ofType {
                        kind
                        name
                        description
                    }
                }
            }
            inputFields {
                name
                description
                type {
                    kind
                    name
                    description
                    ofType {
                        kind
                        name
                        description
                        ofType {
                            kind
                            name
                        }
                    }
                }
            }
        }
    }`
}
