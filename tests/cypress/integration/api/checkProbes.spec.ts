import { apollo } from '../../support/apollo'
import { healthCheck } from '../../support/gql'

describe('xxx', () => {
    it('Check healthcheck', () => {
        healthCheck('LOW', apollo()).its('status').should('eq', 'RED')
    })
})
