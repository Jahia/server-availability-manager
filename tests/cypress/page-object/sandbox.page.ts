import { BasePage } from './base.page'

class SandboxPage extends BasePage {
    elements = {
        sandbox: "[data-sel-role*='sandbox']",
    }

    goTo() {
        cy.visit('/jahia/dashboard', { failOnStatusCode: false })
        cy.get(this.elements.sandbox).click()
        return this
    }
}

export const sandboxPage = new SandboxPage()
