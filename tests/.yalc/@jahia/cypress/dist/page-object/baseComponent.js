"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.BaseComponent = void 0;
class BaseComponent {
    constructor(selector) {
        this.id = BaseComponent.count++;
        this.selector = selector + ' ';
        this.element = cy.get(selector).as('component' + this.id);
    }
    getSelector() {
        return this.selector;
    }
    get() {
        return cy.get('@component' + this.id, { log: false });
    }
}
exports.BaseComponent = BaseComponent;
BaseComponent.count = 0;
