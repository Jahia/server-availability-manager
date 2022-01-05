"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.IFrame = void 0;
const baseComponent_1 = require("./baseComponent");
class IFrame extends baseComponent_1.BaseComponent {
    constructor(selector = 'iframe') {
        super(selector);
        this.get()
            .should(f => {
            const fr = f[0];
            expect(fr.contentWindow.location.href).not.equals('about:blank');
            expect(fr.contentWindow.document.readyState).equals('complete');
            expect(fr.contentDocument.body).not.be.empty;
        })
            .its('0.contentDocument.body').as('framebody' + this.id);
    }
    getBody() {
        return cy.get('@framebody' + this.id);
    }
    enter() {
        this.get().then(f => {
            const fr = f[0];
            cy.visit(fr.contentWindow.location.href);
        });
    }
}
exports.IFrame = IFrame;
