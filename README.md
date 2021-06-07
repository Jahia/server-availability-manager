<!--
    Template for Readmes, see alternatives/examples here: https://github.com/matiassingers/awesome-readme
-->

<!--
    Badges provides a quick glance at the state of the repository and pointers to external resources.
    More can be generated from here: https://shields.io/
-->

|           |                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| --------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Module    | ![ID](https://img.shields.io/badge/ID-server--availability--manager-blue) ![Store](https://img.shields.io/badge/Jahia%20Store-No-red)                                                                                                                                                                                                                                                                                                                                 |
| Tests     | [![Case Management](https://img.shields.io/badge/Case%20Management-Testrail-blue)](https://jahia.testrail.net/index.php?/projects/overview/23)                                                                                                                                                                                                                                                                                                  |
| CI / CD   | [![CircleCI](https://circleci.com/gh/Jahia/server-availability-manager/tree/main.svg?style=shield&circle-token=39c03d3dfded99b563093be92e4506f68ec087e5)](https://app.circleci.com/pipelines/github/Jahia/server-availability-manager) ![Unit Tests](https://img.shields.io/badge/Unit%20Tests-No-red) ![Integration Tests](https://img.shields.io/badge/Integration%20Tests-Yes-brightgreen) ![Build Snapshot](https://img.shields.io/badge/Build%20Snapshot-Yes-brightgreen) ![Build Release](https://img.shields.io/badge/Build%20Release-Yes-brightgreen) |
| Artifacts | [![Snapshot](https://img.shields.io/badge/Snapshot-Nexus-blue)](https://devtools.jahia.com/nexus/content/repositories/jahia-snapshots/org/jahia/modules/server-availability-manager/) [![Release](https://img.shields.io/badge/Release-Nexus-blue)](https://devtools.jahia.com/nexus/content/repositories/jahia-releases/org/jahia/modules/server-availability-manager/)                                                                                                                |
| Slack     | [![Discussion](https://img.shields.io/badge/Discussion-%23module--serverbusy-blue)](https://jahia.slack.com/archives/C022NFUACLR) [![Notifications](https://img.shields.io/badge/Notifications-%23product--team--qa--notifications-blue)](https://jahia.slack.com/archives/CSMQ0DRHA)                                                                                                                                                              |

<a href="https://www.jahia.com/">
    <img src="https://www.jahia.com/modules/jahiacom-templates/images/jahia-3x.png" alt="Jahia logo" title="Jahia" align="right" height="60" />
</a>
 
<!--
    Project name can either be the full length project name (if there is one) or just the repo name. For example: Digital Experience Manager.
-->
 
# Server Availability Manager

Jahia Server Availability Manager is a module extending our GraphQL API to provide additional functionalities associated with server management and server health.

The following core features are part of the module:

* List critical background tasks currently running
* Shutdown the server
* List status of healthcheck probes

## Monitoring running tasks

During its regular lifecycle Jahia will be performing actions that shouldn't be interrupted by maintenance activities (server shutdown, database maintenance, ...). By exposing such tasks, Jahia makes third party platforms (or individual) aware of when interruptions should be avoided.

The following query returns the list of critical tasks currently running on the server.

```graphql
query {
  admin {
    jahia {
      tasks {
        service # Name of the service holding the task
        name    # Name of the tasks that should not be interrupted
        started # Datetime at which the task started (if available)
      }
    }
  }
}
```

This query returns the tasks running at the time the query was made, the server availability manager does not keep a log of previously running tasks.

### Registry

The module is equipped with a registry of running tasks allowing modules, not part of Jahia default distribution, to declare their own tasks. This can also be extended to external infrastructure willing to prevent a server from being restarted

#### Register tasks from a Java module

 To register long-running tasks we use Jahia `FrameworkService` and `TaskRegisterEventHandler` of server availability manager.
 It's done by three steps: 
 1) Before registering the task it is advised to unregister it first just in case it wasn't cleaned up due to the failure:
   `FrameworkService.sendEvent(UNREGISTER_EVENT, constructTaskDetailsEvent(workspace, WORKSPACE_INDEXATION), true);`, where `UNREGISTER_EVENT` represents
    event topic - `org/jahia/modules/sam/TaskRegistryService/UNREGISTER`, and `constructTaskDetailsEvent` creates a map with three event properties: 
    - name
    - service
    - started, in this example we register workspace indexation of augmented search, example can be seen in `ESService.java` class
    
 2) Register the task when starting, same approach as unregister, just `REGISTER` instead of `UNREGISTER`
 3) Unregister the task when ended

Important information: tasks are distinguished using combination of name and service, so this combination should be unique

#### Register tasks via GraphQL API

When external services (nor directly running on a Jahia server) need to let this server know that it shouldn't be restarted, the GraphQL API can be used to create and delete tasks.

```graphql
mutation {
  admin {
    jahia {
      createTask(service: "DevOps Team" name: "Network maintenance on Core VPC")
    }
  }
}
```

Using `deleteTask` with the same parameters will delete that particular task. The registry is shared between GraphQL and Java modules, so you can very well create a task in a Java module and delete it via the GraphQL API.

## Server shutdown

Working jointly with the tasks registry, a shutdown service is exposed via the GraphQL API. 

This API node should be used with care since it actually shutdowns the Jahia server.

```graphql
mutation {
  admin {
    jahia {
      shutdown(
        # When dryRun is provided, the server will not be shutdown but 
        # still return the expected API response (true or false) 
        dryRun: true,   
        timeout: 25,    # In seconds, maximum time to wait for server to be ready (empty list of tasks) to shutdown 
        force: true     # Force immediate shutdown even tasks are running
      )
    }
  }
}
```

The above query is provided as an example, `timeout` and `force` shouldn't be used together since `force` will trigger immediate shutdown without consideration for the `timeout` value.

## Monitoring health

The module also provides health information about the server, in replacement of the [previous healthcheck module](https://github.com/Jahia/healthcheck). 

```graphql
query {
  admin {
    jahia {
      healthCheck(severity: LOW) {  # You can specify the minimum severity to return
        status          # Highest reported status across all probes
        probes {
          name          # Name of the probe
          status        # Status reported by the probe (GREEN to RED)
          severity      # Severity of the probe (LOW to CRITICAL)
          description   # Description specified by the developer of the probe
        }
      }
    }
  }
}
```

Although the module comes with a set of preconfigured probes, additional probes can easily be added.

### REST API

The list of probes are also available via a REST API call (GET) at the following url: [https://{YOUR_JAHIA_HOST}/modules/healthcheck?severity=low](https://{YOUR_JAHIA_HOST}/modules/healthcheck?severity=low)

Configuration is available in [karaf/etc/org.jahia.modules.sam.healthcheck.HealthCheckServlet.cfg](./src/main/resources/META-INF/configurations/org.jahia.modules.sam.healthcheck.HealthCheckServlet.cfg)

```cfg
# default severity level with "?severity=LEVEL" is not provided
severity.default=MEDIUM

# Threshold above which an HTTP error code will be returned
status.threshold=RED
# Error code to be returned if above threshold
status.code=503
```

### (Un)Register and configure probes

Probes are declared in [karaf/etc/org.jahia.modules.sam.healthcheck.ProbesRegistry.cfg](./src/main/resources/META-INF/configurations/org.jahia.modules.sam.healthcheck.ProbesRegistry.cfg), allowing their severity and status to be adjusted based on need.

```cfg
probes.testProbe.severity=HIGH
probes.testProbe.status=RED
```


