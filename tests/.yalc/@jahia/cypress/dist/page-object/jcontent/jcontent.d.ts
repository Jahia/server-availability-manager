import { Accordion, SecondaryNav, Table } from "../moonstone";
import { BasePage } from "../basePage";
export declare class JContent extends BasePage {
    secondaryNav: SecondaryNav;
    accordion: Accordion;
    table: Table;
    static visit(site: string, language: string, path: string): JContent;
    constructor();
    getTable(): Table;
    select(accordion: string): void;
}
