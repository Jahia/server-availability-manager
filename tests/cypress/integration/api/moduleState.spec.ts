import { apollo } from '../../support/apollo'
import {
    healthCheck,
    startingModules,
    stoppingModules,
    changeHealthCheckProperty,
    deleteHealthCheckProperty,
} from '../../support/gql'

describe('Module state probe test', () => {
    it('Check that module state probe is all green with no whitelists or blacklists', () => {
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status.health).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status.health).to.eq('GREEN')
            expect(moduleStateProbe.severity).to.eq('MEDIUM')
        })
    })

    it('Checks the module state probe is YELLOW when two versions installed with only one running', () => {
        cy.downloadAndInstallModuleFromStore('article', '2.0.2')
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status.health).to.eq('YELLOW')
            expect(r.status.message).to.contain('article')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status.health).to.eq('YELLOW')
            expect(moduleStateProbe.status.message).to.contain('article')
        })
        cy.uninstallModule('article', '2.0.2')
    })

    it('Checks that module state probe is in RED after stopping the module', () => {
        stoppingModules('channels', '7.2.1')
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status.health).to.eq('RED')
            expect(r.status.message).to.contain('channels')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status.health).to.eq('RED')
        })
        startingModules('channels', '7.2.1')
    })

    it('Check that module state probe is green after we blacklist the module', () => {
        changeHealthCheckProperty('probes.ModuleState.blacklist', 'channels')
        healthCheck('MEDIUM', apollo()).should((r) => {
            expect(r.status.health).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status.health).to.eq('GREEN')
        })
        deleteHealthCheckProperty('probes.ModuleState.blacklist')
    })

    it('Checks that module state probe is GREEN even after we whitelisted the PAT module', () => {
        changeHealthCheckProperty('probes.ModuleState.whitelist', 'channels')
        healthCheck('MEDIUM', apollo()).should((r) => {
            expect(r.status.health).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status.health).to.eq('GREEN')
        })
        deleteHealthCheckProperty('probes.ModuleState.whitelist')
    })

    after('Start location module back', () => {
        startingModules('seo', '7.2.0')
        startingModules('channels', '7.2.1')
        deleteHealthCheckProperty('probes.ModuleState.blacklist')
        deleteHealthCheckProperty('probes.ModuleState.whitelist')
        cy.uninstallModule('article', '2.0.2')
    })
})
