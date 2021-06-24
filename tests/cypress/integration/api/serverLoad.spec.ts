import { apollo } from '../../support/apollo'
import { healthCheck } from '../../support/gql'

describe('Server Load probe test', () => {
    let setDefaultThreshold: string
    let setYellowThreshold: string
    let setRedThreshold: string

    before('load graphql file and create test dataset', () => {
        setDefaultThreshold = require('../../fixtures/serverLoadProbe/set-default-threshold.json')
        setYellowThreshold = require('../../fixtures/serverLoadProbe/set-yellow-threshold.json')
        setRedThreshold = require('../../fixtures/serverLoadProbe/set-red-threshold.json')

        cy.runProvisioningScript(setDefaultThreshold)
    })

    it('Check that server load probe is all green with default threshold parameters', () => {
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status).to.eq('GREEN')
            const serverLoadProbe = r.probes.find((probe) => probe.name === 'ServerLoad')
            expect(serverLoadProbe.status).to.eq('GREEN')
            expect(serverLoadProbe.severity).to.eq('HIGH')
        })
    })

    it('Checks that server load probe is in YELLOW after changing the threshold to 0', () => {
        cy.runProvisioningScript(setYellowThreshold)
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status).to.eq('YELLOW')
            const serverLoadProbe = r.probes.find((probe) => probe.name === 'ServerLoad')
            expect(serverLoadProbe.status).to.eq('YELLOW')
        })
    })

    it('Checks that server load probe is in RED after changing the threshold to -1', () => {
        cy.runProvisioningScript(setRedThreshold)
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status).to.eq('RED')
            const serverLoadProbe = r.probes.find((probe) => probe.name === 'ServerLoad')
            expect(serverLoadProbe.status).to.eq('RED')
        })
    })

    after('Set server load threshold back to the default', () => {
        cy.runProvisioningScript(setDefaultThreshold)
    })
})
