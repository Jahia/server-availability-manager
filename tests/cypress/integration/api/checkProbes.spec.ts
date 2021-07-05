import { apollo } from '../../support/apollo'
import { healthCheck } from '../../support/gql'

describe('Health check', () => {
    const disableProbe = () => {
        const sshCommands = [
            'config:edit org.jahia.modules.sam.healthcheck.ProbesRegistry',
            'config:property-set probes.testProbe.severity IGNORED',
            'config:property-set probes.testProbe.status GREEN',
            'config:update',
        ]
        cy.task('sshCommand', sshCommands).then((response: string) => {
            cy.log('SSH commands executed:')
            cy.log(JSON.stringify(sshCommands))
            cy.log('Response')
            cy.log(JSON.stringify(response))
        })
    }

    const enableProbe = () => {
        const sshCommands = [
            'config:edit org.jahia.modules.sam.healthcheck.ProbesRegistry',
            'config:property-set probes.testProbe.severity HIGH',
            'config:property-set probes.testProbe.status RED',
            'config:update',
        ]
        cy.task('sshCommand', sshCommands).then((response: string) => {
            cy.log('SSH commands executed:')
            cy.log(JSON.stringify(sshCommands))
            cy.log('Response')
            cy.log(JSON.stringify(response))
        })
    }

    afterEach(() => {
        disableProbe()
    })

    it('Check healthcheck when everything is fine', () => {
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status).to.eq('GREEN')
            expect(r.probes.length).to.eq(4)
        })
    })

    it('Check healthcheck with one HIGH probe RED, asking low', () => {
        enableProbe()
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status).to.eq('RED')
            expect(r.probes.length).to.eq(5)
        })
    })

    it('Check healthcheck with one HIGH probe RED, asking critical', () => {
        enableProbe()
        healthCheck('CRITICAL', apollo()).should((r) => {
            expect(r.status).to.eq('GREEN')
            expect(r.probes.length).to.eq(2)
        })
    })

    it('Check healthcheck servlet when everything is fine', () => {
        cy.request({
            url: `${Cypress.config().baseUrl}/modules/healthcheck`,
            auth: {
                user: 'root',
                pass: Cypress.env('SUPER_USER_PASSWORD'),
                sendImmediately: true,
            },
        }).should((response) => {
            expect(response.body.status).to.eq('GREEN')
            expect(response.status).to.eq(200)
        })
    })

    it('Check healthcheck servlet with one HIGH probe RED, default severity, should return 503', () => {
        enableProbe()
        cy.request({
            url: `${Cypress.config().baseUrl}/modules/healthcheck`,
            auth: {
                user: 'root',
                pass: Cypress.env('SUPER_USER_PASSWORD'),
                sendImmediately: true,
            },
            failOnStatusCode: false,
        }).should((response) => {
            expect(response.body.status).to.eq('RED')
            expect(response.status).to.eq(503)
        })
    })

    it('Check healthcheck servlet with one HIGH probe RED, asking critical', () => {
        enableProbe()
        cy.request({
            url: `${Cypress.config().baseUrl}/modules/healthcheck?severity=critical`,
            auth: {
                user: 'root',
                pass: Cypress.env('SUPER_USER_PASSWORD'),
                sendImmediately: true,
            },
        }).should((response) => {
            expect(response.body.status).to.eq('GREEN')
            expect(response.status).to.eq(200)
        })
    })
})
