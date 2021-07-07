import { apollo } from '../../support/apollo'
import { healthCheck, setDefaultThreshold, setYellowThreshold, setRedThreshold } from '../../support/gql'

describe('Server Load probe test', () => {
    it('Check that server load probe is all green with default threshold parameters', () => {
        setDefaultThreshold()
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
            const serverLoadProbe = r.probes.find((probe) => probe.name == 'ServerLoad')
            expect(serverLoadProbe.status).to.eq('YELLOW')
        })
    })

    it('Checks that server load probe is in RED after changing the threshold to -1', () => {
        setRedThreshold()
        healthCheck('LOW', apollo()).should((r) => {
            const serverLoadProbe = r.probes.find((probe) => probe.name === 'ServerLoad')
            expect(serverLoadProbe.status).to.eq('RED')
            expect(r.status).to.eq('RED')
        })
    })
})
