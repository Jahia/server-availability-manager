import { home } from '../page-object/home.page'
import { sandboxPage } from '../page-object/sandbox.page'

describe('Successfully navigates to sandbox app', () => {
    it('successfully navigate to the app', () => {
        cy.visit('/', { failOnStatusCode: false })
        home.login('root', Cypress.env('SUPER_USER_PASSWORD'), true)

        sandboxPage.goTo()
        expect(sandboxPage.getByText('button', 'Some Action')).not.to.be.undefined
    })
})
