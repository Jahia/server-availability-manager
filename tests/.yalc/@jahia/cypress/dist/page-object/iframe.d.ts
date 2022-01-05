/// <reference types="cypress" />
import { BaseComponent } from "./baseComponent";
import Chainable = Cypress.Chainable;
export declare class IFrame extends BaseComponent {
    private body;
    constructor(selector?: string);
    getBody(): Chainable<JQuery>;
    enter(): void;
}
