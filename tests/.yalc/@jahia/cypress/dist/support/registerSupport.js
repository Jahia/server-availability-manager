"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.registerSupport = void 0;
const apollo_1 = require("./apollo");
const provisioning_1 = require("./provisioning");
const login_1 = require("./login");
const logout_1 = require("./logout");
const installLogsCollector_1 = __importDefault(require("cypress-terminal-report/src/installLogsCollector"));
const fixture_1 = require("./fixture");
const registerSupport = () => {
    Cypress.Commands.add('apolloClient', apollo_1.apolloClient);
    Cypress.Commands.add('apollo', { prevSubject: 'optional' }, apollo_1.apollo);
    Cypress.Commands.add('runProvisioningScript', provisioning_1.runProvisioningScript);
    Cypress.Commands.add('executeGroovy', provisioning_1.executeGroovy);
    Cypress.Commands.add('login', login_1.login);
    Cypress.Commands.add('logout', logout_1.logout);
    Cypress.Commands.overwrite('fixture', fixture_1.fixture);
    installLogsCollector_1.default();
};
exports.registerSupport = registerSupport;
