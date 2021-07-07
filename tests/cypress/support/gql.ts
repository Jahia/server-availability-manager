/* eslint-disable @typescript-eslint/no-explicit-any */
import { ApolloClient, NormalizedCacheObject } from '@apollo/client/core'
import Chainable = Cypress.Chainable
import { apollo } from './apollo'

export const createTask = (
    taskService: string,
    taskName: string,
    apolloClient: ApolloClient<NormalizedCacheObject> = apollo(),
): Chainable<any> => {
    return cy.apolloMutate(apolloClient, {
        variables: {
            service: taskService,
            name: taskName,
        },
        errorPolicy: 'all',
        mutation: require(`graphql-tag/loader!../fixtures/createTask.graphql`),
    })
}

export const deleteTask = (
    taskService: string,
    taskName: string,
    apolloClient: ApolloClient<NormalizedCacheObject> = apollo(),
): Chainable<any> => {
    return cy.apolloMutate(apolloClient, {
        variables: {
            service: taskService,
            name: taskName,
        },
        errorPolicy: 'all',
        mutation: require(`graphql-tag/loader!../fixtures/deleteTask.graphql`),
    })
}

export const healthCheck = (
    severity: string,
    apolloClient: ApolloClient<NormalizedCacheObject> = apollo(),
): Chainable<any> => {
    return cy
        .apolloQuery(apolloClient, {
            query: require(`graphql-tag/loader!../fixtures/healthcheck.graphql`),
            variables: {
                severity,
            },
        })
        .then((response: any) => {
            return response.data.admin.jahia.healthCheck
        })
}

export const load = (
    interval: string,
    apolloClient: ApolloClient<NormalizedCacheObject> = apollo(),
): Chainable<any> => {
    return cy
        .apolloQuery(apolloClient, {
            query: require(`graphql-tag/loader!../fixtures/load.graphql`),
            variables: {
                interval,
            },
        })
        .then((response: any) => {
            console.log(response)
            return response.data.admin.jahia.load
        })
}

const sshCommandExecutor = (sshCommands): any => {
    cy.task('sshCommand', sshCommands).then((response: string) => {
        cy.log('SSH commands executed:')
        cy.log(JSON.stringify(sshCommands))
        cy.log('Response')
        cy.log(JSON.stringify(response))
    })
    // eslint-disable-next-line cypress/no-unnecessary-waiting
    cy.wait(2000)
}

export const changeHealthCheckProperty = (property: string, value: string): any => {
    const sshCommands = [
        'config:edit org.jahia.modules.sam.healthcheck.ProbesRegistry',
        'config:property-set ' + property + ' ' + value,
        'config:update',
    ]
    sshCommandExecutor(sshCommands)
}
export const deleteHealthCheckProperty = (property: string): any => {
    const sshCommands = [
        'config:edit org.jahia.modules.sam.healthcheck.ProbesRegistry',
        'config:property-delete ' + property,
        'config:update',
    ]
    sshCommandExecutor(sshCommands)
}

export const stoppingModules = (module: string, version: string): any => {
    cy.log('Stopping ' + module + '/' + version)
    cy.request({
        url: `${Cypress.config().baseUrl}/modules/api/bundles/org.jahia.modules/` + module + `/` + version + `/_stop`,
        method: 'POST',
        body: { data: '' },
        headers: { 'content-type': 'application/x-www-form-urlencoded' },
        auth: {
            user: 'root',
            pass: Cypress.env('SUPER_USER_PASSWORD'),
            sendImmediately: true,
        },
    })
    // eslint-disable-next-line cypress/no-unnecessary-waiting
    cy.wait(2000)
}

export const startingModules = (module: string, version: string): any => {
    cy.log('Starting ' + module + '/' + version)
    cy.request({
        url: `${Cypress.config().baseUrl}/modules/api/bundles/org.jahia.modules/` + module + `/` + version + `/_start`,
        method: 'POST',
        body: { data: '' },
        headers: { 'content-type': 'application/x-www-form-urlencoded' },
        auth: {
            user: 'root',
            pass: Cypress.env('SUPER_USER_PASSWORD'),
            sendImmediately: true,
        },
    })
    // eslint-disable-next-line cypress/no-unnecessary-waiting
    cy.wait(2000)
}

export const setDefaultThreshold = (): any => {
    const sshCommands = [
        'config:edit org.jahia.modules.sam.healthcheck.ProbesRegistry',
        'config:property-set probes.ServerLoad.requestLoadYellowThreshold 40',
        'config:property-set probes.ServerLoad.requestLoadRedThreshold 70',
        'config:property-set probes.ServerLoad.sessionLoadYellowThreshold 40',
        'config:property-set probes.ServerLoad.sessionLoadRedThreshold 70',
        'config:update',
    ]
    sshCommandExecutor(sshCommands)
}

export const setYellowThreshold = (): any => {
    const sshCommands = [
        'config:edit org.jahia.modules.sam.healthcheck.ProbesRegistry',
        'config:property-set probes.ServerLoad.requestLoadYellowThreshold -1',
        'config:property-set probes.ServerLoad.requestLoadRedThreshold 70',
        'config:property-set probes.ServerLoad.sessionLoadYellowThreshold -1',
        'config:property-set probes.ServerLoad.sessionLoadRedThreshold 70',
        'config:update',
    ]
    sshCommandExecutor(sshCommands)
}

export const setRedThreshold = (): any => {
    const sshCommands = [
        'config:edit org.jahia.modules.sam.healthcheck.ProbesRegistry',
        'config:property-set probes.ServerLoad.requestLoadYellowThreshold -1',
        'config:property-set probes.ServerLoad.requestLoadRedThreshold -1',
        'config:property-set probes.ServerLoad.sessionLoadYellowThreshold -1',
        'config:property-set probes.ServerLoad.sessionLoadRedThreshold -1',
        'config:update',
    ]
    sshCommandExecutor(sshCommands)
}

export const disableProbe = (): any => {
    const sshCommands = [
        'config:edit org.jahia.modules.sam.healthcheck.ProbesRegistry',
        'config:property-set probes.testProbe.severity IGNORED',
        'config:property-set probes.testProbe.status GREEN',
        'config:update',
    ]
    sshCommandExecutor(sshCommands)
}

export const enableProbe = (): any => {
    const sshCommands = [
        'config:edit org.jahia.modules.sam.healthcheck.ProbesRegistry',
        'config:property-set probes.testProbe.severity HIGH',
        'config:property-set probes.testProbe.status RED',
        'config:update',
    ]
    sshCommandExecutor(sshCommands)
}
