import { load } from '../../support/gql'

describe('Load average', () => {
    it('Returns a load value', () => {
        load('ONE').should((r) => {
            expect(r.requests.count).to.be.gte(1)
            expect(r.requests.average).to.be.gte(0)
            expect(r.sessions.count).to.be.gte(1)
            expect(r.sessions.average).to.be.gte(0)
        })

        load('FIVE').should((r) => {
            expect(r.requests.average).to.be.gte(0)
            expect(r.sessions.average).to.be.gte(0)
        })

        load('FIFTEEN').should((r) => {
            expect(r.requests.average).to.be.gte(0)
            expect(r.sessions.average).to.be.gte(0)
        })
    })
})
