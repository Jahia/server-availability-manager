"use strict";
/* eslint-disable @typescript-eslint/no-explicit-any, @typescript-eslint/no-namespace */
var __rest = (this && this.__rest) || function (s, e) {
    var t = {};
    for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p) && e.indexOf(p) < 0)
        t[p] = s[p];
    if (s != null && typeof Object.getOwnPropertySymbols === "function")
        for (var i = 0, p = Object.getOwnPropertySymbols(s); i < p.length; i++) {
            if (e.indexOf(p[i]) < 0 && Object.prototype.propertyIsEnumerable.call(s, p[i]))
                t[p[i]] = s[p[i]];
        }
    return t;
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.apollo = void 0;
const graphql_tag_1 = __importDefault(require("graphql-tag"));
function isQuery(options) {
    return options.query !== undefined;
}
function isQueryFile(options) {
    return options.queryFile !== undefined;
}
function isMutationFile(options) {
    return options.mutationFile !== undefined;
}
const apollo = function (apollo, options) {
    if (!apollo) {
        apollo = this.currentApolloClient;
    }
    let result;
    let logger;
    if (!apollo) {
        cy.apolloClient().apollo(options);
    }
    else {
        if (isQueryFile(options)) {
            const { queryFile } = options, apolloOptions = __rest(options, ["queryFile"]);
            cy.fixture(queryFile).then(content => {
                cy.apollo(Object.assign({ query: graphql_tag_1.default(content) }, apolloOptions));
            });
        }
        else if (isMutationFile(options)) {
            const { mutationFile } = options, apolloOptions = __rest(options, ["mutationFile"]);
            cy.fixture(mutationFile).then(content => {
                cy.apollo(Object.assign({ mutation: graphql_tag_1.default(content) }, apolloOptions));
            });
        }
        else {
            const { log = true } = options, apolloOptions = __rest(options, ["log"]);
            if (log) {
                logger = Cypress.log({
                    autoEnd: false,
                    name: 'apollo',
                    displayName: 'apollo',
                    message: isQuery(apolloOptions) ? `Execute Graphql Query: ${apolloOptions.query.loc.source.body}` : `Execute Graphql Mutation: ${apolloOptions.mutation.loc.source.body}`,
                    consoleProps: () => {
                        return {
                            Options: apolloOptions,
                            Yielded: result
                        };
                    },
                });
            }
            cy.wrap({}, { log: false })
                .then(() => (isQuery(options) ? apollo.query(options) : apollo.mutate(options))
                .then(r => {
                result = r;
                logger === null || logger === void 0 ? void 0 : logger.end();
                return r;
            }));
        }
    }
};
exports.apollo = apollo;
