const healthcheck = (qs) => {
    return cy.request({
        url: `${Cypress.config().baseUrl}/modules/healthcheck`,
        qs,
        headers: {
            referer: Cypress.config().baseUrl
        },
        auth: {
            user: 'root',
            pass: Cypress.env('SUPER_USER_PASSWORD'),
            sendImmediately: true
        },
        failOnStatusCode: false
    });
};

describe('healthcheck REST API test', () => {
    it('should return using "includes" parameter', () => {
        console.log("Return with no filter");
        healthcheck({}).should(response => {
            expect(response.status).to.eq(200);
            expect(response.body.probes).to.not.be.empty;
        });

        console.log("Return with empty filter");
        healthcheck({includes: undefined}).should(response => {
            expect(response.status).to.eq(200);
            expect(response.body.probes).to.not.be.empty;
        });

        console.log("Return one probe");
        healthcheck({includes: "FileDatastore"}).should(response => {
            expect(response.status).to.eq(200);
            expect(response.body.probes?.length).to.eq(1);
            expect(response.body.probes[0]?.name).to.eq('FileDatastore');
        });

        console.log("Return more than one probe");
        healthcheck({includes: "FileDatastore,DBConnectivity"}).should(response => {
            expect(response.status).to.eq(200);
            expect(response.body.probes.length).to.be.eq(2);
            const probeNames = response.body.probes?.map(r => r.name);
            expect('FileDatastore').to.be.oneOf(probeNames);
            expect('DBConnectivity').to.be.oneOf(probeNames);
        });

        console.log("Filter with only invalid probe");
        healthcheck({includes: "UndefinedProbe"}).should(response => {
            expect(response.status).to.eq(200);
            expect(response.body.probes).to.be.empty;
        });

        console.log("Filter invalid probe");
        healthcheck({includes: "FileDatastore,UndefinedProbe"}).should(response => {
            expect(response.status).to.eq(200);
            expect(response.body.probes.length).to.be.eq(1);
            const probeNames = response.body.probes?.map(r => r.name);
            expect('FileDatastore').to.be.oneOf(probeNames);
        });
    });

});
