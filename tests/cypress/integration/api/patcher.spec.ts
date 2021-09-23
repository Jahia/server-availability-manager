import { apollo } from '../../support/apollo'
import { healthCheck } from '../../support/gql'

describe('Server Load probe test', () => {
    let skipScript
    let failScript

    before('Setup script', () => {
        cy.fixture('patcherProbe/skipPatch.groovy').then((file) => (skipScript = file))
        cy.fixture('patcherProbe/failPatch.groovy').then((file) => (failScript = file))
    })

    it('Check that patcher  probe is all green after startup', () => {
        cy.runGroovyScript(skipScript)
        healthCheck('CRITICAL', apollo()).should((r) => {
            expect(r.status.health).to.eq('GREEN')
            const serverLoadProbe = r.probes.find((probe) => probe.name === 'PatchFailures')
            expect(serverLoadProbe.status.health).to.eq('GREEN')
            expect(serverLoadProbe.severity).to.eq('CRITICAL')
        })
    })

    it('Check that patcher  probe is red after failing a patch', () => {
        cy.runGroovyScript(failScript)
        healthCheck('CRITICAL', apollo()).should((r) => {
            expect(r.status.health).to.eq('RED')
            const serverLoadProbe = r.probes.find((probe) => probe.name === 'PatchFailures')
            expect(serverLoadProbe.status.health).to.eq('RED')
            expect(serverLoadProbe.severity).to.eq('CRITICAL')
        })
    })

    after('Restoring state', () => {
        cy.runGroovyScript(skipScript)
    })
})
