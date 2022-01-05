"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.registerCommands = void 0;
const apollo_1 = require("./apollo");
const provisioning_1 = require("./provisioning");
const login_1 = require("./login");
const logout_1 = require("./logout");
// import installLogsCollector from 'cypress-terminal-report/src/installLogsCollector'
const registerCommands = () => {
    Cypress.Commands.add('apolloClient', apollo_1.apolloClient);
    Cypress.Commands.add('apolloQuery', { prevSubject: 'optional' }, apollo_1.apolloQuery);
    Cypress.Commands.add('apolloMutate', { prevSubject: 'optional' }, apollo_1.apolloMutate);
    Cypress.Commands.add('runProvisioningScript', provisioning_1.runProvisioningScript);
    Cypress.Commands.add('executeGroovy', provisioning_1.executeGroovy);
    Cypress.Commands.add('login', login_1.login);
    Cypress.Commands.add('logout', logout_1.logout);
    // installLogsCollector();
};
exports.registerCommands = registerCommands;
