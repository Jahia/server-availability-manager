"use strict";
/* eslint-disable @typescript-eslint/no-explicit-any, @typescript-eslint/no-namespace */
Object.defineProperty(exports, "__esModule", { value: true });
exports.installBundle = void 0;
/// <reference types="cypress" />
const installBundle = function (bundleFile) {
    cy.runProvisioningScript({
        fileContent: '- installBundle: "' + bundleFile + '"',
        type: 'application/yaml'
    }, [{
            fileName: bundleFile,
            type: 'text/plain'
        }]);
};
exports.installBundle = installBundle;
