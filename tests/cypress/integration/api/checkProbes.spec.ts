import { apollo } from '../../support/apollo'
import gql from 'graphql-tag'
import { healthCheck } from '../../support/gql'

describe('xxx', () => {
    it('Check healthcheck', () => {
        healthCheck('LOW', apollo()).its('data.admin.serverAvailabilityManager.healthCheck.status').should('eq', 'RED')
    })
})
