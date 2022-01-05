"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.PrimaryNav = void 0;
const baseComponent_1 = require("../baseComponent");
class PrimaryNav extends baseComponent_1.BaseComponent {
    constructor() {
        super('.moonstone-primaryNav');
    }
    click(itemName) {
        this.get().find(`.moonstone-primaryNavItem[role="${itemName}"]`).click();
    }
    listItems() {
        return this.get().find('.moonstone-primaryNavItem').then(items => {
            return Array.prototype.slice.call(items, 0).map(i => i.attributes['role'] ? i.attributes['role'].value : null).filter(i => i !== null);
        });
    }
}
exports.PrimaryNav = PrimaryNav;
