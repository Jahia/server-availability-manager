import { apollo } from '../../support/apollo'
import { healthCheck } from '../../support/gql'

describe('Health check', () => {
    let enableProbe: string
    let disableProbe: string

    before('load graphql file and create test dataset', () => {
        enableProbe = require('../../fixtures/test-enable.json')
        disableProbe = require('../../fixtures/test-disable.json')
    })

    it('Check healthcheck when everything is fine', () => {
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status).to.eq('GREEN')
            expect(r.probes.length).to.eq(1)
        })
    })

    it('Check healthcheck with one HIGH probe RED, asking low', () => {
        cy.runProvisioningScript(enableProbe)
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status).to.eq('RED')
            expect(r.probes.length).to.eq(2)
        })
        cy.runProvisioningScript(disableProbe)
    })

    it('Check healthcheck with one HIGH probe RED, asking critical', () => {
        cy.runProvisioningScript(enableProbe)
        healthCheck('CRITICAL', apollo()).should((r) => {
            expect(r.status).to.eq('GREEN')
            expect(r.probes.length).to.eq(1)
        })
        cy.runProvisioningScript(disableProbe)
    })
})
