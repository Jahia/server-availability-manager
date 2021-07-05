import { apollo } from '../../support/apollo'
import { healthCheck } from '../../support/gql'

describe('Server Load probe test', () => {
    const setDefaultThreshold = () => {
        const sshCommands = [
            'config:edit org.jahia.modules.sam.healthcheck.ProbesRegistry',
            'config:property-set probes.ServerLoad.requestLoadYellowThreshold 40',
            'config:property-set probes.ServerLoad.requestLoadRedThreshold 70',
            'config:property-set probes.ServerLoad.sessionLoadYellowThreshold 40',
            'config:property-set probes.ServerLoad.sessionLoadRedThreshold 70',
            'config:update',
        ]
        cy.task('sshCommand', sshCommands).then((response: string) => {
            cy.log('SSH commands executed:')
            cy.log(JSON.stringify(sshCommands))
            cy.log('Response')
            cy.log(JSON.stringify(response))
        })
    }

    const setYellowThreshold = () => {
        const sshCommands = [
            'config:edit org.jahia.modules.sam.healthcheck.ProbesRegistry',
            'config:property-set probes.ServerLoad.requestLoadYellowThreshold -1',
            'config:property-set probes.ServerLoad.requestLoadRedThreshold 70',
            'config:property-set probes.ServerLoad.sessionLoadYellowThreshold -1',
            'config:property-set probes.ServerLoad.sessionLoadRedThreshold 70',
            'config:update',
        ]
        cy.task('sshCommand', sshCommands).then((response: string) => {
            cy.log('SSH commands executed:')
            cy.log(JSON.stringify(sshCommands))
            cy.log('Response')
            cy.log(JSON.stringify(response))
        })
    }

    const setRedThreshold = () => {
        const sshCommands = [
            'config:edit org.jahia.modules.sam.healthcheck.ProbesRegistry',
            'config:property-set probes.ServerLoad.requestLoadYellowThreshold -1',
            'config:property-set probes.ServerLoad.requestLoadRedThreshold -1',
            'config:property-set probes.ServerLoad.sessionLoadYellowThreshold -1',
            'config:property-set probes.ServerLoad.sessionLoadRedThreshold -1',
            'config:update',
        ]
        cy.task('sshCommand', sshCommands).then((response: string) => {
            cy.log('SSH commands executed:')
            cy.log(JSON.stringify(sshCommands))
            cy.log('Response')
            cy.log(JSON.stringify(response))
        })
    }

    before('load graphql file and create test dataset', () => {
        setDefaultThreshold()
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
        setYellowThreshold()
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status).to.eq('YELLOW')
            const serverLoadProbe = r.probes.find((probe) => probe.name === 'ServerLoad')
            expect(serverLoadProbe.status).to.eq('YELLOW')
        })
    })

    it('Checks that server load probe is in RED after changing the threshold to -1', () => {
        setRedThreshold()
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status).to.eq('RED')
            const serverLoadProbe = r.probes.find((probe) => probe.name === 'ServerLoad')
            expect(serverLoadProbe.status).to.eq('RED')
        })
    })

    after('Set server load threshold back to the default', () => {
        setDefaultThreshold()
    })
})
