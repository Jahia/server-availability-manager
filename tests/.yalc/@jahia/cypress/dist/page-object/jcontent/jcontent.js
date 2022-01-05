"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.JContent = void 0;
const moonstone_1 = require("../moonstone");
const basePage_1 = require("../basePage");
class JContent extends basePage_1.BasePage {
    constructor() {
        super();
        this.secondaryNav = new moonstone_1.SecondaryNav();
        this.accordion = new moonstone_1.Accordion(this.secondaryNav.getSelector());
    }
    static visit(site, language, path) {
        cy.visit(`/jahia/jcontent/${site}/${language}/${path}`);
        return new JContent();
    }
    getTable() {
        if (!this.table) {
            this.table = new moonstone_1.Table();
        }
        return this.table;
    }
    select(accordion) {
        this.accordion.click(accordion);
    }
}
exports.JContent = JContent;
