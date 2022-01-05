"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.TableRow = exports.Table = void 0;
const baseComponent_1 = require("../baseComponent");
const menu_1 = require("./menu");
class Table extends baseComponent_1.BaseComponent {
    constructor(selector = '') {
        super(selector + '.moonstone-Table');
    }
    getRow(i) {
        return new TableRow(`${this.selector} .moonstone-TableBody .moonstone-TableRow:nth-child(${i})`);
    }
}
exports.Table = Table;
class TableRow extends baseComponent_1.BaseComponent {
    constructor(selector) {
        super(selector);
    }
    contextMenu() {
        this.get().rightclick();
        return new menu_1.Menu('#menuHolder .moonstone-menu:not(.moonstone-hidden)');
    }
}
exports.TableRow = TableRow;
