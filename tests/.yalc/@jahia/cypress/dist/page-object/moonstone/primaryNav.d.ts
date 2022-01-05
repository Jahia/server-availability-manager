import { BaseComponent } from "../baseComponent";
import Chainable = Cypress.Chainable;
export declare class PrimaryNav extends BaseComponent {
    constructor();
    click(itemName: string): void;
    listItems(): Chainable<string[]>;
}
