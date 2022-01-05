"use strict";
/* eslint-disable @typescript-eslint/no-explicit-any, @typescript-eslint/no-namespace */
Object.defineProperty(exports, "__esModule", { value: true });
exports.login = void 0;
/// <reference types="cypress" />
const login = (username = 'root', password = Cypress.env('SUPER_USER_PASSWORD')) => {
    Cypress.log({
        name: 'login',
        message: `Login with ${username}`,
        consoleProps: () => {
            return {
                User: username,
            };
        },
    });
    cy.request({
        method: 'POST',
        url: '/cms/login',
        form: true,
        body: { username, password },
        followRedirect: false,
        log: false
    }).then(res => {
        expect(res.status, 'Login result').to.eq(302);
    });
};
exports.login = login;
