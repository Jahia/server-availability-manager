/* eslint max-nested-callbacks: ["error", 6] */

import {getDescriptions} from '@jahia/cypress';

describe('Test for GraphQL schema description', () => {
    // The blacklist is used to ignore nodes that are not registered directly by graphql-core
    // These descriptions should be added in their respective codebases
    const noDescBlacklist = [
        // Currently missing but out of scope for SAM
        'GqlBackgroundJob/jobState/GqlBackgroundJobState'
    ];

    const entryNodes = ['JahiaAdminQuery'];
    entryNodes.forEach(entryNode => {
        it(`Description for all nodes under ${entryNode}`, () => {
            getDescriptions(entryNode).then(result => {
                console.log(result);

                // Get the list of nodes that are missing descriptions
                // Remove the nodes that are in the blacklist
                const noDesc = result
                    .filter((graphqlType => graphqlType.description === null || graphqlType.description.length === 0))
                    .filter((graphqlType => !noDescBlacklist.some(t => graphqlType.nodePath.join('/').includes(t))));

                noDesc.forEach(description => {
                    cy.log(`Missing description for ${description.schemaType} at path: ${description.nodePath.join('/')}`);
                    console.log(`Missing description for ${description.schemaType} at path: ${description.nodePath.join('/')}`);
                });
                cy.then(() => expect(noDesc.length).to.equal(0));

                // Get the list of nodes that are deprecated and ensure an explanation is present (deprecationReason)*
                // "Deprecated" (in all its forms) is not considered a valid deprecationReason
                // Remove the nodes that are in the blacklist
                const noDeprecateReason = result
                    .filter((graphqlType => (graphqlType.isDeprecated === true && (!graphqlType.deprecationReason || graphqlType.deprecationReason.length === 0 || (graphqlType.deprecationReason instanceof String && graphqlType.deprecationReason.toLowercase() === ('Deprecated').toLowerCase())))))
                    .filter((graphqlType => !noDescBlacklist.some(t => graphqlType.nodePath.join('/').includes(t))));

                noDeprecateReason.forEach(description => {
                    cy.log(`Deprecated ${description.schemaType} missing explanation at path: ${description.nodePath.join('/')}`);
                    console.log(`Deprecated ${description.schemaType} missing explanation at path: ${description.nodePath.join('/')}`);
                });
                cy.then(() => expect(noDesc.length).to.equal(0));
            });
        });
    });
});
