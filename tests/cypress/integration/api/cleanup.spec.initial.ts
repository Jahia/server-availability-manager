import { startingModules } from '../../support/gql'

describe('Cleanup env', () => {
    it('Uninstall the module healthcheck', () => {
        cy.uninstallModule('healthcheck', '2.3.1')
    })
    it('Install the module article in 2.0.3', () => {
        cy.downloadAndInstallModuleFromStore('article', '2.0.3')
    })
    it('Fixes start-level of modules', () => {
        cy.sshCommand([
            'bundle:start-level graphql-dxm-provider 90',
            'bundle:start-level server-availability-manager 90',
            'bundle:start-level article 90',
        ])
    })

    it('Starts the module article', () => {
        startingModules('article', '2.0.3')
    })
})
