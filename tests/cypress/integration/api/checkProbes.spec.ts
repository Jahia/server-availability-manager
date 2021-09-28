import { apollo } from '../../support/apollo'
import { healthCheck } from '../../support/gql'

describe('Health check', () => {
    let enableProbe: string
    let disableProbe: string

    before('load graphql file and create test dataset', () => {
        enableProbe = require('../../fixtures/test-enable.json')
        disableProbe = require('../../fixtures/test-disable.json')
    })

    afterEach(() => {
        cy.runProvisioningScript(disableProbe)
    })

    it('Check healthcheck when everything is fine', () => {
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status.health).to.eq('GREEN')
            expect(r.probes.length).to.eq(5)
        })
    })

    it('Check healthcheck with one HIGH probe RED, asking low', () => {
        cy.runProvisioningScript(enableProbe)
        healthCheck('LOW', apollo()).should((r) => {
            expect(r.status.health).to.eq('RED')
            expect(r.probes.length).to.eq(6)
        })
    })

    it('Check healthcheck with one HIGH probe RED, asking critical', () => {
        cy.runProvisioningScript(enableProbe)
        healthCheck('CRITICAL', apollo()).should((r) => {
            expect(r.status.health).to.eq('GREEN')
            expect(r.probes.length).to.eq(3)
        })
    })

    it('Check healthcheck servlet when everything is fine', () => {
        cy.request({
            url: `${Cypress.config().baseUrl}/modules/healthcheck`,
            headers: {
                referer: Cypress.config().baseUrl,
            },
            auth: {
                user: 'root',
                pass: Cypress.env('SUPER_USER_PASSWORD'),
                sendImmediately: true,
            },
        }).should((response) => {
            expect(response.body.status.health).to.eq('GREEN')
            expect(response.status).to.eq(200)
        })
    })

    it('Check healthcheck servlet with one HIGH probe RED, default severity, should return 503', () => {
        cy.runProvisioningScript(enableProbe)
        cy.request({
            url: `${Cypress.config().baseUrl}/modules/healthcheck`,
            headers: {
                referer: Cypress.config().baseUrl,
            },
            auth: {
                user: 'root',
                pass: Cypress.env('SUPER_USER_PASSWORD'),
                sendImmediately: true,
            },
            failOnStatusCode: false,
        }).should((response) => {
            expect(response.body.status.health).to.eq('RED')
            expect(response.status).to.eq(503)
        })
    })

    it('Check healthcheck servlet with one HIGH probe RED, asking critical', () => {
        cy.runProvisioningScript(enableProbe)
        cy.request({
            url: `${Cypress.config().baseUrl}/modules/healthcheck?severity=critical`,
            headers: {
                referer: Cypress.config().baseUrl,
            },
            auth: {
                user: 'root',
                pass: Cypress.env('SUPER_USER_PASSWORD'),
                sendImmediately: true,
            },
        }).should((response) => {
            expect(response.body.status.health).to.eq('GREEN')
            expect(response.status).to.eq(200)
        })
    })
})
