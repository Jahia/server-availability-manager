import { healthCheck } from '../../support/gql'

describe('Module state probe test', () => {
    it('Check that module state probe is all green with no whitelists or blacklists', () => {
        healthCheck('LOW').should((r) => {
            expect(r.status.health).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status.health).to.eq('GREEN')
            expect(moduleStateProbe.severity).to.eq('MEDIUM')
        })
    })

    it('Checks the module state probe is YELLOW when two versions installed with only one running', () => {
        cy.runProvisioningScript({fileName:'moduleStateProbe/install-dashboard.json'})
        healthCheck('LOW').should((r) => {
            expect(r.status.health).to.eq('YELLOW')
            expect(r.status.message).to.contain('jahia-dashboard')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status.health).to.eq('YELLOW')
            expect(moduleStateProbe.status.message).to.contain('jahia-dashboard')
        })
        cy.runProvisioningScript({fileName:'moduleStateProbe/uninstall-dashboard.json'})
    })

    it('Checks that module state probe is in RED after stopping the module', () => {
        cy.runProvisioningScript({fileName:'moduleStateProbe/stop-channels.json'})
        healthCheck('LOW').should((r) => {
            expect(r.status.health).to.eq('RED')
            expect(r.status.message).to.contain('channels')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status.health).to.eq('RED')
        })
    })

    it('Check that module state probe is green after we blacklist the module', () => {
        cy.runProvisioningScript({fileName:'moduleStateProbe/enable-blacklist.json'})
        healthCheck('MEDIUM').should((r) => {
            expect(r.status.health).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status.health).to.eq('GREEN')
        })
    })

    it('Checks that module state probe is GREEN even after we whitelisted the PAT module', () => {
        cy.runProvisioningScript({fileName:'moduleStateProbe/enable-whitelist.json'})
        healthCheck('MEDIUM').should((r) => {
            expect(r.status.health).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status.health).to.eq('GREEN')
        })
    })

    it('Checks the module state probe is RED when we removed blacklist', () => {
        cy.runProvisioningScript({fileName:'moduleStateProbe/disable-blacklist.json'})
        healthCheck('MEDIUM').should((r) => {
            expect(r.status.health).to.eq('RED')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status.health).to.eq('RED')
        })
    })

    it('Checks the module state probe is GREEN when we started channels module and stopped SEO module, which is not inside whitelist', () => {
        cy.runProvisioningScript({fileName:'moduleStateProbe/start-channels.json'})
        cy.runProvisioningScript({fileName:'moduleStateProbe/stop-seo.json'})
        healthCheck('MEDIUM').should((r) => {
            expect(r.status.health).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status.health).to.eq('GREEN')
        })
    })

    after('Start location module back', () => {
        cy.runProvisioningScript({fileName:'moduleStateProbe/start-seo.json'})
        cy.runProvisioningScript({fileName:'moduleStateProbe/start-channels.json'})
        cy.runProvisioningScript({fileName:'moduleStateProbe/disable-blacklist.json'})
        cy.runProvisioningScript({fileName:'moduleStateProbe/disable-whitelist.json'})
    })
})
