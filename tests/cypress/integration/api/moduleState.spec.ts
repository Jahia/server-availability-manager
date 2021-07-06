import { apollo } from '../../support/apollo'
import {
    healthCheck,
    startingModules,
    stoppingModules,
    disableBlacklist,
    enableBlacklist,
    disableWhitelist,
    enableWhitelist,
} from '../../support/gql'

describe('Module state probe test', () => {
    it('Check that module state probe is all green with no whitelists or blacklists', () => {
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status).to.eq('GREEN')
            expect(moduleStateProbe.severity).to.eq('MEDIUM')
        })
    })

    it('Checks that module state probe is in RED after stopping the module', () => {
        stoppingModules('channels', '7.2.1')
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status).to.eq('RED')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status).to.eq('RED')
        })
        startingModules('channels', '7.2.1')
    })

    it('Check that module state probe is green after we blacklist the module', () => {
        enableBlacklist()
        healthCheck('MEDIUM', apollo()).should((r) => {
            expect(r.status).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status).to.eq('GREEN')
        })
        disableBlacklist()
    })

    it('Checks that module state probe is GREEN even after we whitelisted the PAT module', () => {
        enableWhitelist()
        healthCheck('MEDIUM', apollo()).should((r) => {
            expect(r.status).to.eq('GREEN')
            const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
            expect(moduleStateProbe.status).to.eq('GREEN')
        })
        disableWhitelist()
    })

    // it('Checks the module state probe is RED when we remove blacklist', () => {
    //     startingModules('seo', '7.2.0')
    //     startingModules('channels', '7.2.1')
    //     enableBlacklist()
    //     // disableWhitelist()
    //     healthCheck('MEDIUM', apollo()).should((r) => {
    //         expect(r.status).to.eq('RED')
    //         const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
    //         expect(moduleStateProbe.status).to.eq('RED')
    //     })
    // })

    // it('Checks the module state probe is GREEN when we started channels module and stopped SEO module, which is not inside whitelist', () => {
    //     startingModules('channels', '7.2.1')
    //     stoppingModules('seo', '7.2.0')
    //     healthCheck('MEDIUM', apollo()).should((r) => {
    //         expect(r.status).to.eq('GREEN')
    //         const moduleStateProbe = r.probes.find((probe) => probe.name === 'ModuleState')
    //         expect(moduleStateProbe.status).to.eq('GREEN')
    //     })
    // })

    after('Start location module back', () => {
        startingModules('seo', '7.2.0')
        startingModules('channels', '7.2.1')
        disableBlacklist()
        disableWhitelist()
    })
})
