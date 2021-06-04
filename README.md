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

# Register tasks that prevent server from stopping
 To register long-running tasks we use OSGi `EventAdmin` and `TaskRegisterEventHandler` of server availability manager.
 It's done by three steps: 
 1) Instantiate EventAdmin service: `private final EventAdmin eventAdmin = BundleUtils.getOsgiService(EventAdmin.class.getName());`
 2) Before registering the task it is advised to unregister it first just in case it wasn't cleaned up due to the failure:
`eventAdmin.sendEvent(new Event(UNREGISTER_EVENT, constructTaskDetailsEvent(workspace, WORKSPACE_INDEXATION)));`, where `UNREGISTER_EVENT` represents
    event topic - `org/jahia/modules/sam/TaskRegistryService/UNREGISTER`, and `constructTaskDetailsEvent` creates a map with three event properties: 
    - name
    - service
    - started, in this example we register workspace indexation of augmented search, example can be seen in `ESService.java` class
    
 3) Register the task when starting, same approach as unregister, just `REGISTER` instead of `UNREGISTER`
 4) Unregister the task when ended

Important information: tasks are distinguished using combination of name and service, so this combination should be unique
