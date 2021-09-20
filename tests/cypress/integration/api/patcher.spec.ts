import { apollo } from '../../support/apollo'
import { healthCheck } from '../../support/gql'

describe('Server Load probe test', () => {
    let runFailPatchScript: string
    let runSkipPatchScript: string

    before('load graphql file and create test dataset', () => {
        runFailPatchScript = require('../../fixtures/patcherProbe/runFailPatchScript.json')
        runSkipPatchScript = require('../../fixtures/patcherProbe/runSkipPatchScript.json')

        cy.runProvisioningScript(runSkipPatchScript)
    })

    it('Check that patcher  probe is all green after startup', () => {
        healthCheck('CRITICAL', apollo()).should((r) => {
            expect(r.status).to.eq('GREEN')
            const serverLoadProbe = r.probes.find((probe) => probe.name === 'PatchFailures')
            expect(serverLoadProbe.status).to.eq('GREEN')
            expect(serverLoadProbe.severity).to.eq('CRITICAL')
        })
    })

    it('Check that patcher  probe is red after failing a patch', () => {
        cy.runProvisioningScript(runFailPatchScript)
        healthCheck('CRITICAL', apollo()).should((r) => {
            expect(r.status).to.eq('RED')
            const serverLoadProbe = r.probes.find((probe) => probe.name === 'PatchFailures')
            expect(serverLoadProbe.status).to.eq('RED')
            expect(serverLoadProbe.severity).to.eq('CRITICAL')
        })
    })

    after('Restoring state', () => {
        cy.runProvisioningScript(runSkipPatchScript)
    })
})
