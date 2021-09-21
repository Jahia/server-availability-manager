import { apollo } from '../../support/apollo'
import { healthCheck } from '../../support/gql'

describe('Module state probe test', () => {
    let enableWhitelist: string
    let enableBlacklist: string
    let disableWhitelist: string
    let disableBlacklist: string
    let stopChannels: string
    let startChannels: string
    let stopSeoModule: string
    let startSeoModule: string
    let installDashboardModule: string
    let uninstallDashboardModule: string

    before('load graphql file and create test dataset', () => {
        enableWhitelist = require('../../fixtures/moduleStateProbe/enable-whitelist.json')
        enableBlacklist = require('../../fixtures/moduleStateProbe/enable-blacklist.json')
        disableWhitelist = require('../../fixtures/moduleStateProbe/disable-whitelist.json')
        disableBlacklist = require('../../fixtures/moduleStateProbe/disable-blacklist.json')
        stopChannels = require('../../fixtures/moduleStateProbe/stop-channels.json')
        startChannels = require('../../fixtures/moduleStateProbe/start-channels.json')
        startSeoModule = require('../../fixtures/moduleStateProbe/start-seo.json')
        stopSeoModule = require('../../fixtures/moduleStateProbe/stop-seo.json')
        installDashboardModule = require('../../fixtures/moduleStateProbe/install-dashboard.json')
        uninstallDashboardModule = require('../../fixtures/moduleStateProbe/uninstall-dashboard.json')
    })

    it('Check that module state probe is all green with no whitelists or blacklists', () => {
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status.health).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status.health).to.eq('GREEN')
            expect(moduleStateProbe.severity).to.eq('MEDIUM')
        })
    })

    it('Checks the module state probe is YELLOW when two versions installed with only one running', () => {
        cy.runProvisioningScript(installDashboardModule)
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status.health).to.eq('YELLOW')
            expect(r.status.message).to.contain('jahia-dashboard')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status.health).to.eq('YELLOW')
            expect(moduleStateProbe.status.message).to.contain('jahia-dashboard')
        })
        cy.runProvisioningScript(uninstallDashboardModule)
    })

    it('Checks that module state probe is in RED after stopping the module', () => {
        cy.runProvisioningScript(stopChannels)
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status.health).to.eq('RED')
            expect(r.status.message).to.contain('channels')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status.health).to.eq('RED')
        })
    })

    it('Check that module state probe is green after we blacklist the module', () => {
        cy.runProvisioningScript(enableBlacklist)
        healthCheck('MEDIUM', apollo()).should((r) => {
            expect(r.status.health).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status.health).to.eq('GREEN')
        })
    })

    it('Checks that module state probe is GREEN even after we whitelisted the PAT module', () => {
        cy.runProvisioningScript(enableWhitelist)
        healthCheck('MEDIUM', apollo()).should((r) => {
            expect(r.status.health).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status.health).to.eq('GREEN')
        })
    })

    it('Checks the module state probe is RED when we removed blacklist', () => {
        cy.runProvisioningScript(disableBlacklist)
        healthCheck('MEDIUM', apollo()).should((r) => {
            expect(r.status.health).to.eq('RED')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status.health).to.eq('RED')
        })
    })

    it('Checks the module state probe is GREEN when we started channels module and stopped SEO module, which is not inside whitelist', () => {
        cy.runProvisioningScript(startChannels)
        cy.runProvisioningScript(stopSeoModule)
        healthCheck('MEDIUM', apollo()).should((r) => {
            expect(r.status.health).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status.health).to.eq('GREEN')
        })
    })

    after('Start location module back', () => {
        cy.runProvisioningScript(startSeoModule)
        cy.runProvisioningScript(startChannels)
        cy.runProvisioningScript(disableBlacklist)
        cy.runProvisioningScript(disableWhitelist)
    })
})
