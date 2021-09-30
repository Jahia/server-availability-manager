import { changeHealthCheckProperty, startingModules } from '../../support/gql'

describe('Cleanup env', () => {
    it('Uninstall the module healthcheck', () => {
        cy.uninstallModule('healthcheck', '2.3.1')
    })
})
