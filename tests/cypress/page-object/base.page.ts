export class BasePage {
    /**
     * Get any element of given type that contain given text
     * It does not require to be the direct element containing text
     * example: <span><div>mytext</div></span> getByText("span", "myText") will work
     * @param type of content to find
     * @param text to find
     */
    getByText(type: string, text: string): Cypress.Chainable {
        return cy.contains(type, text)
    }
}
