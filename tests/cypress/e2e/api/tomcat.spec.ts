import {healthCheck} from '../../support/gql';

describe('Supported stack tomcat probe test', () => {
    it('Checks tomcat version', () => {
        healthCheck('LOW', ['SupportedStackTomcat']).should(r => {
            expect(r.status.health).to.eq('GREEN');
            expect(r.probes.length).to.be.eq(1);
            expect(r.probes[0].name).to.be.eq('SupportedStackTomcat');
        });
    });
});
