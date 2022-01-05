"use strict";
/* eslint-disable @typescript-eslint/no-explicit-any, @typescript-eslint/no-namespace */
Object.defineProperty(exports, "__esModule", { value: true });
exports.executeGroovy = void 0;
/// <reference types="cypress" />
const executeGroovy = function (scriptFile) {
    cy.runProvisioningScript({
        fileContent: '- executeScript: "' + scriptFile + '"',
        type: 'application/yaml'
    }, [{
            fileName: scriptFile,
            type: 'text/plain'
        }]).then(r => r[0]);
};
exports.executeGroovy = executeGroovy;
