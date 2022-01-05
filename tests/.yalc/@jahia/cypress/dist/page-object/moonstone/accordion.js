"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.Accordion = void 0;
const baseComponent_1 = require("../baseComponent");
class Accordion extends baseComponent_1.BaseComponent {
    constructor(selector = '') {
        super(selector + '.moonstone-accordion');
    }
    click(itemName) {
        this.get().find(`section.moonstone-accordionItem header[aria-controls="${itemName}"]`).click();
    }
    listItems() {
        return this.get().find('section.moonstone-accordionItem header').then(items => {
            return Array.prototype.slice.call(items, 0).map(i => i.attributes['aria-controls'] ? i.attributes['aria-controls'].value : null).filter(i => i !== null);
        });
    }
    getContent() {
        return this.get().find('section.moonstone-accordionItem .moonstone-accordionItem_content');
    }
}
exports.Accordion = Accordion;
