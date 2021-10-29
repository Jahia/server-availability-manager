# v1.1.1 (Fri Oct 29 2021)

#### 🐛 Bug Fix

- BACKLOG-16425 - Fix dependencies [#59](https://github.com/Jahia/server-availability-manager/pull/59) ([@rknj](https://github.com/rknj))
- BACKLOG-16425 - Use latest orb version and added back the nightly test against latest… [#58](https://github.com/Jahia/server-availability-manager/pull/58) ([@rknj](https://github.com/rknj))
- QA-13829 Add additional data to health check [#53](https://github.com/Jahia/server-availability-manager/pull/53) ([@AKarmanov](https://github.com/AKarmanov))
- QA-13829 Update message for overall probe [#52](https://github.com/Jahia/server-availability-manager/pull/52) ([@AKarmanov](https://github.com/AKarmanov))
- BACKLOG-16381: Fixed message for startlevel) [#51](https://github.com/Jahia/server-availability-manager/pull/51) ([@tdraier](https://github.com/tdraier))
- BACKLOG-16291 Add messages to probes [#49](https://github.com/Jahia/server-availability-manager/pull/49) ([@AKarmanov](https://github.com/AKarmanov))
- BACKLOG-16316: Add new PatcherProbe detecting if any migration patch failed [#45](https://github.com/Jahia/server-availability-manager/pull/45) ([@cedmail](https://github.com/cedmail))
- QA-13838: Add scope, fixed content-length issue [#47](https://github.com/Jahia/server-availability-manager/pull/47) ([@tdraier](https://github.com/tdraier))
- BACKLOG-16205: Add check for startlevel in modules probe [#46](https://github.com/Jahia/server-availability-manager/pull/46) ([@tdraier](https://github.com/tdraier))
- TECH-210: Remove stable release image for integration tests for now [#44](https://github.com/Jahia/server-availability-manager/pull/44) ([@gflores-jahia](https://github.com/gflores-jahia))
- TECH-210: Update GraphQL dependencies [#43](https://github.com/Jahia/server-availability-manager/pull/43) ([@gflores-jahia](https://github.com/gflores-jahia))
- Fixing checkProbes servlet tests [#42](https://github.com/Jahia/server-availability-manager/pull/42) ([@rfonseca85](https://github.com/rfonseca85))
- Trying with an updated key [#40](https://github.com/Jahia/server-availability-manager/pull/40) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Better handling of errors, add scope for healthcheck [#39](https://github.com/Jahia/server-availability-manager/pull/39) ([@tdraier](https://github.com/tdraier) [@Fgerthoffert](https://github.com/Fgerthoffert))
- BACKLOG-16120 - Release SAM for Jahia 8 [#38](https://github.com/Jahia/server-availability-manager/pull/38) ([@rfonseca85](https://github.com/rfonseca85))

#### ⚠️ Pushed to `main`

- Updated the orb version used ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Removed doc from readme and added a link to the corresponding page on the Academy ([@Fgerthoffert](https://github.com/Fgerthoffert))
- [ci skip] Updated signature for Server Availability Manager ([@jahia-ci](https://github.com/jahia-ci))

#### Authors: 8

- Alex Karmanov ([@AKarmanov](https://github.com/AKarmanov))
- Cedric Mailleux ([@cedmail](https://github.com/cedmail))
- Francois G. ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Geofrey Flores ([@gflores-jahia](https://github.com/gflores-jahia))
- Jahia Continuous Integration account ([@jahia-ci](https://github.com/jahia-ci))
- Michael De Checchi ([@rknj](https://github.com/rknj))
- Rafael Fonseca ([@rfonseca85](https://github.com/rfonseca85))
- Thomas Draier ([@tdraier](https://github.com/tdraier))

---

# v0.0.1 (Wed Jun 30 2021)

#### 🐛 Bug Fix

- Trying with an updated key [#40](https://github.com/Jahia/server-availability-manager/pull/40) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Better handling of errors, add scope for healthcheck [#39](https://github.com/Jahia/server-availability-manager/pull/39) ([@tdraier](https://github.com/tdraier) [@Fgerthoffert](https://github.com/Fgerthoffert))
- BACKLOG-16120 - Release SAM for Jahia 8 [#38](https://github.com/Jahia/server-availability-manager/pull/38) ([@rfonseca85](https://github.com/rfonseca85))
- Feature/ns update provisioning env run script [#36](https://github.com/Jahia/server-availability-manager/pull/36) ([@nazsaquib](https://github.com/nazsaquib) [@Fgerthoffert](https://github.com/Fgerthoffert))
- Adding ServerLoadProbe configuration to the cfg file [#35](https://github.com/Jahia/server-availability-manager/pull/35) ([@rfonseca85](https://github.com/rfonseca85))
- BACKLOG-16122 - Implement server-availability-manager createTask cypress test for guest user [#34](https://github.com/Jahia/server-availability-manager/pull/34) ([@rfonseca85](https://github.com/rfonseca85))
- BACKLOG-16133 add module state example configs [#33](https://github.com/Jahia/server-availability-manager/pull/33) ([@kovunov](https://github.com/kovunov))
- BACKLOG-16067: Moved fields [#32](https://github.com/Jahia/server-availability-manager/pull/32) ([@tdraier](https://github.com/tdraier) [@kovunov](https://github.com/kovunov))
- Another round of update to incorporate recent changes [#29](https://github.com/Jahia/server-availability-manager/pull/29) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- BACKLOG-16111 - Implement the system load probe [#28](https://github.com/Jahia/server-availability-manager/pull/28) ([@rfonseca85](https://github.com/rfonseca85))
- BACKLOG-16113 implement modules probe [#31](https://github.com/Jahia/server-availability-manager/pull/31) ([@kovunov](https://github.com/kovunov))
- Removed a redundant line from the env.run.sh script [#30](https://github.com/Jahia/server-availability-manager/pull/30) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- BACKLOG-16124 implemented thread dump string matching [#25](https://github.com/Jahia/server-availability-manager/pull/25) ([@kovunov](https://github.com/kovunov) [@tdraier](https://github.com/tdraier))
- BACKLOG-16130 - Fixing issue when installing SAM for the very first time [#27](https://github.com/Jahia/server-availability-manager/pull/27) ([@rfonseca85](https://github.com/rfonseca85))
- [BACKLOG-16123] - Change server-availability-manager deleteTask to return false when task not found [#22](https://github.com/Jahia/server-availability-manager/pull/22) ([@rfonseca85](https://github.com/rfonseca85))
- BACKLOG-16132 - First pass on documentation [#24](https://github.com/Jahia/server-availability-manager/pull/24) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- BACKLOG-16112: Added server load info [#26](https://github.com/Jahia/server-availability-manager/pull/26) ([@tdraier](https://github.com/tdraier))
- BACKLOG-16036 update readme with EventAdmin usage guidelines [#20](https://github.com/Jahia/server-availability-manager/pull/20) ([@kovunov](https://github.com/kovunov))
- BACKLOG-16110 - Implementing Datastore probe [#23](https://github.com/Jahia/server-availability-manager/pull/23) ([@rfonseca85](https://github.com/rfonseca85))
- BACKLOG-16105: Added servlet alias [#21](https://github.com/Jahia/server-availability-manager/pull/21) ([@tdraier](https://github.com/tdraier))
- BACKLOG-16106, BACKLOG-16108 : Add probes [#16](https://github.com/Jahia/server-availability-manager/pull/16) ([@tdraier](https://github.com/tdraier))
- BACKLOG-16066 - Updated tests to remove flakiness [#18](https://github.com/Jahia/server-availability-manager/pull/18) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- BACKLOG0-16066 - Added missing tests [#17](https://github.com/Jahia/server-availability-manager/pull/17) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- BACKLOG-16066 - Partial implementation of the service-availability-manager tests [#15](https://github.com/Jahia/server-availability-manager/pull/15) ([@rfonseca85](https://github.com/rfonseca85))
- Updated the readme to fix the badges and add a one-liner describing the module [#13](https://github.com/Jahia/server-availability-manager/pull/13) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- BACKLOG-16037 - Fixed the task creation [#11](https://github.com/Jahia/server-availability-manager/pull/11) ([@rfonseca85](https://github.com/rfonseca85))
- Add enterprise repositories to settings.xml [#12](https://github.com/Jahia/server-availability-manager/pull/12) ([@kovunov](https://github.com/kovunov))
- BACKLOG-16036 implemented event handlers for registering and unregistering, appended registry tasks to task list [#9](https://github.com/Jahia/server-availability-manager/pull/9) ([@kovunov](https://github.com/kovunov))
- BACKLOG-16065: Moved to private repository [#10](https://github.com/Jahia/server-availability-manager/pull/10) ([@tdraier](https://github.com/tdraier))
- BACKLOG-16037 - Create a GraphQL node to mutate the registry [#8](https://github.com/Jahia/server-availability-manager/pull/8) ([@rfonseca85](https://github.com/rfonseca85))
- BACKLOG-16034: shutdown command [#7](https://github.com/Jahia/server-availability-manager/pull/7) ([@tdraier](https://github.com/tdraier))
- Updated GraphQL schema descriptions [#6](https://github.com/Jahia/server-availability-manager/pull/6) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- [BACKLOG-16035] - Creating a registry allowing other Jahia modules to submit their tasks [#5](https://github.com/Jahia/server-availability-manager/pull/5) ([@rfonseca85](https://github.com/rfonseca85))
- BACKLOG-16030 list active tasks on server [#3](https://github.com/Jahia/server-availability-manager/pull/3) ([@kovunov](https://github.com/kovunov))
- Updating repo and configuring CI/CD [#1](https://github.com/Jahia/server-availability-manager/pull/1) ([@rfonseca85](https://github.com/rfonseca85))

#### ⚠️ Pushed to `main`

- Update config.yml ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Update README.md ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Updating project basic configuration ([@rfonseca85](https://github.com/rfonseca85))
- Initial commit ([@rfonseca85](https://github.com/rfonseca85))

#### Authors: 5

- Anton Kovunov ([@kovunov](https://github.com/kovunov))
- Francois G. ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Nazmus Saquib ([@nazsaquib](https://github.com/nazsaquib))
- Rafael Fonseca ([@rfonseca85](https://github.com/rfonseca85))
- Thomas Draier ([@tdraier](https://github.com/tdraier))

---

@default
Please update this file with the repository's changelog.
