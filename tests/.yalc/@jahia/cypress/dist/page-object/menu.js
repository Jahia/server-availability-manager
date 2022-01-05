"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.Menu = void 0;
const baseComponent_1 = require("./baseComponent");
class Menu extends baseComponent_1.BaseComponent {
    constructor(selector) {
        super(selector);
    }
    select(item) {
        cy.get(this.selector).find(`.moonstone-menuItem[data-sel-role="${item}"]`).trigger('click');
    }
}
exports.Menu = Menu;
