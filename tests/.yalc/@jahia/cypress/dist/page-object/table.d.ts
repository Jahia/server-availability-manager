import { BaseComponent } from "./baseComponent";
import { Menu } from "./menu";
export declare class Table extends BaseComponent {
    constructor(selector?: string);
    getRow(i: number): TableRow;
}
export declare class TableRow extends BaseComponent {
    constructor(selector: string);
    contextMenu(): Menu;
}
