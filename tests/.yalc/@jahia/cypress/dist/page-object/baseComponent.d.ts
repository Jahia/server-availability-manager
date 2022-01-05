/// <reference types="cypress" />
import Chainable = Cypress.Chainable;
export declare class BaseComponent {
    static count: number;
    element: Chainable<JQuery>;
    id: number;
    selector: string;
    constructor(selector: string);
    getSelector(): string;
    get(): Chainable<JQuery>;
}
