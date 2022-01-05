/// <reference types="cypress" />
import { BaseComponent } from "./baseComponent";
import Chainable = Cypress.Chainable;
export declare class Accordion extends BaseComponent {
    constructor(selector?: string);
    click(itemName: string): void;
    listItems(): Chainable<string[]>;
    getContent(): Chainable<JQuery>;
}
