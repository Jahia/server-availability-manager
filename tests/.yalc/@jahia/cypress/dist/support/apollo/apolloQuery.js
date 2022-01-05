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
Object.defineProperty(exports, "__esModule", { value: true });
exports.apolloQuery = void 0;
const apolloQuery = function (apollo, options) {
    if (!apollo) {
        apollo = this.currentApolloClient;
    }
    let result;
    let logger;
    const { log = true } = options, apolloOptions = __rest(options, ["log"]);
    if (!apollo) {
        cy.apolloClient().apolloQuery(apolloOptions);
    }
    else {
        if (log) {
            logger = Cypress.log({
                autoEnd: false,
                name: 'apolloQuery',
                displayName: 'apQuery',
                message: `Execute Graphql Query: ${options.query.loc.source.body}`,
                consoleProps: () => {
                    return {
                        Options: apolloOptions,
                        Yielded: result
                    };
                },
            });
        }
        cy.wrap({}, { log: false })
            .then(() => apollo.query(options))
            .then(r => {
            result = r;
            logger === null || logger === void 0 ? void 0 : logger.end();
            return r;
        });
    }
};
exports.apolloQuery = apolloQuery;
