import { BasePage } from './base.page'

class HomePage extends BasePage {
    elements = {
        loginBtn: '.clearfix > .topbar-list > :nth-child(2) > #login',
        loginForm: 'form[name="loginForm"]',
        loginFormUsername: '#username',
        loginFormPassword: '#password',
        loginFormRememberMe: '#useCookie',
        headerLogin: '.clearfix > .topbar-list > :nth-child(2) > #login',
        headerUsername: '.user-box > .dropdown-toggle',
        headerUserDropdown: '.dropdown-toggle > .caret',
        headerUserDropdownLogout: '.clearfix > .topbar-list > .user-box > .topbar-dropdown > :nth-child(3) > a',
    }

    goTo() {
        cy.goTo('/')
        return this
    }

    login(username: string, password: string, rememberMe: boolean) {
        cy.get(this.elements.loginBtn).click()
        cy.get(this.elements.loginFormUsername).type(username)
        cy.get(this.elements.loginFormPassword).type(password)
        if (rememberMe) {
            cy.get(this.elements.loginFormRememberMe).check()
        } else {
            cy.get(this.elements.loginFormRememberMe).uncheck()
        }
        cy.get(this.elements.loginForm).submit()
        cy.get(this.elements.headerUsername)
            .invoke('text')
            .should((value) => {
                expect(value.replace(/[^0-9a-z]/gi, '').toLowerCase()).to.eq(username)
            })
    }

    logout() {
        cy.get(this.elements.headerUserDropdown).click()
        cy.get(this.elements.headerUserDropdownLogout).click()
        cy.get(this.elements.headerLogin)
            .invoke('text')
            .should((value) => {
                expect(value.replace(/[^0-9a-z]/gi, '').toLowerCase()).to.eq('login')
            })
    }
}

export const home = new HomePage()
