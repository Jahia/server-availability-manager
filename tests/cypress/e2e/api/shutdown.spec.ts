import {createTask, deleteTask} from '../../support/gql';

describe('Shutdown via API - mutation.admin.jahia.shutdown', () => {
    it('Shutdown with no tasks running (dryRun)', function () {
        cy.apollo({
            variables: {
                dryRun: true
            },
            mutationFile: 'shutdown.graphql'
        })
            .its('data.admin.jahia.shutdown')
            .should('eq', true);
    });

    it('Shutdown impossible with tasks running (dryRun) - should exhaust default timeout (25s)', function () {
        createTask('service1', 'name1');
        const startShutdown = new Date().getTime();
        cy.apollo({
            variables: {
                dryRun: true
            },
            mutationFile: 'shutdown.graphql'
        }).then(response => {
            cy.log('Requested shutdown');
            expect(response.data.admin.jahia.shutdown).to.be.false;
            const completeShutdown = new Date().getTime();
            const executionTime = completeShutdown - startShutdown;
            cy.log(`Execution time: ${executionTime}`);
            expect(executionTime).to.be.greaterThan(25000);
            expect(executionTime).not.to.be.greaterThan(28000);
        });
        deleteTask('service1', 'name1');
    });

    it('Shutdown impossible with tasks running (dryRun) - shorter timeout (2s)', function () {
        createTask('service1', 'name1');
        const startShutdown = new Date().getTime();
        cy.apollo({
            variables: {
                dryRun: true,
                timeout: 2
            },
            mutationFile: 'shutdown.graphql'
        }).then(response => {
            cy.log('Requested shutdown');
            expect(response.data.admin.jahia.shutdown).to.be.false;
            const completeShutdown = new Date().getTime();
            const executionTime = completeShutdown - startShutdown;
            cy.log(`Execution time: ${executionTime}`);
            expect(executionTime).to.be.greaterThan(2000);
            expect(executionTime).not.to.be.greaterThan(5000);
        });
        deleteTask('service1', 'name1');
    });

    it('Force shutdown without tasks running (dryRun)', function () {
        const startShutdown = new Date().getTime();
        cy.apollo({
            variables: {
                dryRun: true,
                force: true
            },
            mutationFile: 'shutdown.graphql'
        }).then(response => {
            expect(response.data.admin.jahia.shutdown).to.be.true;
            const completeShutdown = new Date().getTime();
            const executionTime = completeShutdown - startShutdown;
            cy.log(`Execution time: ${executionTime}`);
            expect(executionTime).not.to.be.greaterThan(2000);
        });
    });

    it('Force shutdown with tasks running (dryRun)', function () {
        createTask('service1', 'name1');
        const startShutdown = new Date().getTime();
        cy.apollo({
            variables: {
                dryRun: true,
                force: true
            },
            mutationFile: 'shutdown.graphql'
        }).then(response => {
            cy.log('Requested shutdown');
            expect(response.data.admin.jahia.shutdown).to.be.true;
            const completeShutdown = new Date().getTime();
            const executionTime = completeShutdown - startShutdown;
            cy.log(`Execution time: ${executionTime}`);
            expect(executionTime).not.to.be.greaterThan(2000);
        });
        deleteTask('service1', 'name1');
    });
});
