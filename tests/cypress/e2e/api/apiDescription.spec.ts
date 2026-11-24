/* eslint max-nested-callbacks: ["error", 6] */

import {getDescriptions, getStartedModuleVersion} from '@jahia/cypress';

describe('Test for GraphQL schema description', () => {
    // The blacklist is used to ignore nodes that are not registered directly by graphql-core
    // These descriptions should be added in their respective codebases
    const noDescBlacklist = [
        // Currently missing but out of scope for SAM
        'GqlBackgroundJob'
    ];

    // The jcontent versions 3.6.0 to 3.6.2 leave these two arguments without a description, and jcontent 3.6.3 describes them.
    // Each entry is the last two segments of a node path, because every type that implements JCRNode has the field.
    // Remove this exclusion once jahia/jahia-ee:8 ships jcontent 3.6.3: https://github.com/Jahia/server-availability-manager/issues/280
    const jcontentVersionsWithoutDescription = /^3\.6\.[0-2]$/;
    const jcontentArgumentsWithoutDescription = [
        'findAvailableNodeName/nodeType',
        'findAvailableNodeName/language'
    ];

    const entryNodes = ['JahiaAdminQuery'];
    entryNodes.forEach(entryNode => {
        it(`Description for all nodes under ${entryNode}`, () => {
            let excludedArguments: string[] = [];
            getStartedModuleVersion('jcontent').then((jcontentVersion: string | undefined) => {
                excludedArguments = jcontentVersion !== undefined && jcontentVersionsWithoutDescription.test(jcontentVersion) ? jcontentArgumentsWithoutDescription : [];
                cy.task('log', `jcontent ${jcontentVersion ?? 'not started'}: excluded arguments [${excludedArguments.join(', ')}]`);
            });

            getDescriptions(entryNode).then(result => {
                console.log(result);

                // Get the list of nodes that are missing descriptions
                // Remove the nodes that are in the blacklist, and the jcontent arguments excluded above
                const noDesc = result
                    .filter((graphqlType => graphqlType.description === null || graphqlType.description.length === 0))
                    .filter((graphqlType => !noDescBlacklist.some(t => graphqlType.nodePath.join('/').includes(t))))
                    .filter((graphqlType => !excludedArguments.includes(graphqlType.nodePath.slice(-2).join('/'))));

                noDesc.forEach(description => {
                    cy.log(`Missing description for ${description.schemaType} at path: ${description.nodePath.join('/')}`);
                    console.log(`Missing description for ${description.schemaType} at path: ${description.nodePath.join('/')}`);
                });
                cy.then(() => expect(noDesc.length).to.equal(0));

                // Get the list of nodes that are deprecated and ensure an explanation is present (deprecationReason)*
                // "Deprecated" (in all its forms) is not considered a valid deprecationReason
                // Remove the nodes that are in the blacklist
                const noDeprecateReason = result
                    .filter((graphqlType => (graphqlType.isDeprecated === true && (!graphqlType.deprecationReason || graphqlType.deprecationReason.length === 0 || (typeof graphqlType.deprecationReason === 'string' && graphqlType.deprecationReason.toLowerCase() === 'deprecated')))))
                    .filter((graphqlType => !noDescBlacklist.some(t => graphqlType.nodePath.join('/').includes(t))));

                noDeprecateReason.forEach(description => {
                    cy.log(`Deprecated ${description.schemaType} missing explanation at path: ${description.nodePath.join('/')}`);
                    console.log(`Deprecated ${description.schemaType} missing explanation at path: ${description.nodePath.join('/')}`);
                });
                cy.then(() => expect(noDeprecateReason.length).to.equal(0));
            });
        });
    });
});
