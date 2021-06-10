import { apollo } from '../../support/apollo'
import { healthCheck } from '../../support/gql'

describe('Module state probe test', () => {
    let enableWhitelist: string
    let enableBlacklist: string
    let disableWhitelist: string
    let disableBlacklist: string
    let stopLocation: string
    let startLocation: string

    before('load graphql file and create test dataset', () => {
        enableWhitelist = require('../../fixtures/moduleStateProbe/enable-whitelist.json')
        enableBlacklist = require('../../fixtures/moduleStateProbe/enable-blacklist.json')
        disableWhitelist = require('../../fixtures/moduleStateProbe/disable-whitelist.json')
        disableBlacklist = require('../../fixtures/moduleStateProbe/disable-blacklist.json')
        stopLocation = require('../../fixtures/moduleStateProbe/stop-location.json')
        startLocation = require('../../fixtures/moduleStateProbe/start-location.json')
    })

    it('Check that module state probe is all green with no whitelists or blacklists', () => {
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status).to.eq('GREEN')
            expect(moduleStateProbe.severity).to.eq('MEDIUM')
        })
    })

    it('Checks that module state probe is in RED after stopping the module', () => {
        cy.runProvisioningScript(stopLocation)
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status).to.eq('RED')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status).to.eq('RED')
        })
    })

    it('Check that module state probe is green after we blacklist the module', () => {
        cy.runProvisioningScript(enableBlacklist)
        healthCheck('MEDIUM', apollo()).should((r) => {
            expect(r.status).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status).to.eq('GREEN')
        })
    })

    it('Checks that module state probe is RED even after we whitelisted the PAT module', () => {
        cy.runProvisioningScript(enableWhitelist)
        healthCheck('MEDIUM', apollo()).should((r) => {
            expect(r.status).to.eq('RED')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status).to.eq('RED')
        })
    })

    it('Checks the module state probe is GREEN when we removed blacklist', () => {
        cy.runProvisioningScript(disableBlacklist)
        healthCheck('MEDIUM', apollo()).should((r) => {
            expect(r.status).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status).to.eq('GREEN')
        })
    })

    after('Start location module back', () => {
        cy.runProvisioningScript(disableBlacklist)
        cy.runProvisioningScript(disableWhitelist)
        cy.runProvisioningScript(startLocation)
    })
})
