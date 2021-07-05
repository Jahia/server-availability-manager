import { apollo } from '../../support/apollo'
import { healthCheck } from '../../support/gql'

describe('Module state probe test', () => {
    const changeHealthCheckProperty = (property, value) => {
        const sshCommands = [
            'config:edit org.jahia.modules.sam.healthcheck.ProbesRegistry',
            'config:property-set ' + property + ' ' + value,
            'config:update',
        ]
        cy.task('sshCommand', sshCommands).then((response: string) => {
            cy.log('SSH commands executed:')
            cy.log(JSON.stringify(sshCommands))
            cy.log('Response')
            cy.log(JSON.stringify(response))
        })
    }
    const enableBlacklist = () => {
        changeHealthCheckProperty('probes.ModuleState.blacklist', 'channels')
    }
    const enableWhitelist = () => {
        changeHealthCheckProperty('probes.ModuleState.whitelist', 'channels')
    }
    const disableBlacklist = () => {
        changeHealthCheckProperty('probes.ModuleState.blacklist', 'none')
    }
    const disableWhitelist = () => {
        changeHealthCheckProperty('probes.ModuleState.whitelist', 'none')
    }
    const stopChannels = () => {
        cy.log('Stopping Channels')
    }
    const startChannels = () => {
        cy.log('Starting Channels')
    }
    const stopSeoModule = () => {
        cy.log('Stopping SeoModule')
    }
    const startSeoModule = () => {
        cy.log('Starting SeoModule')
    }

    it('Check that module state probe is all green with no whitelists or blacklists', () => {
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status).to.eq('GREEN')
            expect(moduleStateProbe.severity).to.eq('MEDIUM')
        })
    })

    it('Checks that module state probe is in RED after stopping the module', () => {
        stopChannels()
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status).to.eq('RED')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status).to.eq('RED')
        })
    })

    it('Check that module state probe is green after we blacklist the module', () => {
        enableBlacklist()
        healthCheck('MEDIUM', apollo()).should((r) => {
            expect(r.status).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status).to.eq('GREEN')
        })
    })

    it('Checks that module state probe is GREEN even after we whitelisted the PAT module', () => {
        enableWhitelist()
        healthCheck('MEDIUM', apollo()).should((r) => {
            expect(r.status).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status).to.eq('GREEN')
        })
    })

    it('Checks the module state probe is RED when we remove blacklist', () => {
        disableBlacklist()
        healthCheck('MEDIUM', apollo()).should((r) => {
            expect(r.status).to.eq('RED')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status).to.eq('RED')
        })
    })

    it('Checks the module state probe is GREEN when we started channels module and stopped SEO module, which is not inside whitelist', () => {
        startChannels()
        stopSeoModule()
        healthCheck('MEDIUM', apollo()).should((r) => {
            expect(r.status).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status).to.eq('GREEN')
        })
    })

    after('Start location module back', () => {
        startSeoModule()
        startChannels()
        disableBlacklist()
        disableWhitelist()
    })
})
