"use strict";
/* eslint-disable @typescript-eslint/no-explicit-any, @typescript-eslint/no-namespace */
Object.defineProperty(exports, "__esModule", { value: true });
exports.runProvisioningScript = void 0;
/// <reference types="cypress" />
function getBlob(formFile) {
    return new Promise(resolve => {
        if (formFile.fileContent) {
            resolve(new Blob([formFile.fileContent], { type: formFile.type }));
        }
        else {
            cy.fixture(formFile.fileName, 'binary').then(content => {
                formFile.fileContent = content;
                resolve(Cypress.Blob.binaryStringToBlob(content, formFile.type));
            });
        }
    });
}
const runProvisioningScript = (script, files, options = { log: true }) => {
    const formData = new FormData();
    getBlob(script).then(blob => formData.append("script", blob));
    files.forEach((f) => {
        getBlob(f).then(blob => {
            formData.append("file", blob, f.fileName);
        });
    });
    let response;
    let result;
    let logger;
    if (options.log) {
        logger = Cypress.log({
            autoEnd: false,
            name: 'runProvisioningScript',
            displayName: 'provScript',
            message: `Run ${script.fileName ? script.fileName : 'inline script'}`,
            consoleProps: () => {
                return {
                    Script: script,
                    Files: files,
                    Response: response,
                    Yielded: result
                };
            },
        });
    }
    cy.request({
        url: `${Cypress.config().baseUrl}/modules/api/provisioning`,
        method: 'POST',
        auth: {
            user: 'root',
            pass: Cypress.env('SUPER_USER_PASSWORD'),
            sendImmediately: true,
        },
        body: formData,
        log: false
    }).then(res => {
        response = res;
        expect(res.status, 'Script result').to.eq(200);
        const decoder = new TextDecoder();
        result = JSON.parse(decoder.decode(response.body));
        logger === null || logger === void 0 ? void 0 : logger.end();
        return result;
    });
};
exports.runProvisioningScript = runProvisioningScript;
