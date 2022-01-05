"use strict";
/* eslint-disable @typescript-eslint/no-explicit-any, @typescript-eslint/no-namespace */
Object.defineProperty(exports, "__esModule", { value: true });
exports.logout = void 0;
/// <reference types="cypress" />
const logout = () => {
    Cypress.log({
        name: 'logout',
        message: `Logout`,
        consoleProps: () => {
            return {};
        },
    });
    cy.request({
        method: 'POST',
        url: '/cms/logout',
        followRedirect: false,
        log: false
    }).then(res => {
        expect(res.status, 'Logout result').to.eq(302);
    });
};
exports.logout = logout;
