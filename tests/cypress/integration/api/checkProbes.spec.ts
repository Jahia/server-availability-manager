import { healthCheck } from '../../support/gql'

describe('Health check', () => {
    afterEach(() => {
        cy.runProvisioningScript({ fileName: 'test-disable.json' })
    })

    it('Check healthcheck when everything is fine', () => {
        healthCheck('LOW').should((r) => {
            expect(r.status.health).to.eq('GREEN')
            expect(r.probes.length).to.be.gte(6)
        })
    })

    it('Check healthcheck with one HIGH probe RED, asking low', () => {
        cy.runProvisioningScript({ fileName: 'test-enable.json' })
        healthCheck('LOW').should((r) => {
            expect(r.status.health).to.eq('RED')
            expect(r.probes.length).to.be.gte(7)
        })
    })

    it('Check healthcheck with one HIGH probe RED, asking critical', () => {
        cy.runProvisioningScript({ fileName: 'test-enable.json' })
        healthCheck('CRITICAL').should((r) => {
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
            expect(response.body.probes.length).to.be.gte(6)
            expect(response.status).to.eq(200)
        })
    })

    it('Check healthcheck servlet with one HIGH probe RED, default severity, should return 503', () => {
        cy.runProvisioningScript({ fileName: 'test-enable.json' })
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
            expect(response.body.probes.length).to.be.gte(7)
            expect(response.status).to.eq(503)
        })
    })

    it('Check healthcheck servlet with one HIGH probe RED, asking critical', () => {
        cy.runProvisioningScript({ fileName: 'test-enable.json' })
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
            expect(response.body.probes.length).to.eq(3)
            expect(response.status).to.eq(200)
        })
    })
})
