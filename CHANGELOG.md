# v1.6.2 (Thu Apr 29 2021)

#### 🐛 Bug Fix

- Updated the tests environment to latest changes [#80](https://github.com/Jahia/sandbox/pull/80) ([@tdraier](https://github.com/tdraier) [@Fgerthoffert](https://github.com/Fgerthoffert))
- Migration to jahia-modules-orb - Step 1: Build commands and jobs [#78](https://github.com/Jahia/sandbox/pull/78) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Migration to jahia-modules-orb - Step 0: Setup & SonarQube [#76](https://github.com/Jahia/sandbox/pull/76) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Fixed security issue with ssri [#77](https://github.com/Jahia/sandbox/pull/77) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Integrate recent CI updates to the sandbox module [#73](https://github.com/Jahia/sandbox/pull/73) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Removed timeout from config [#72](https://github.com/Jahia/sandbox/pull/72) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- BACKLOG-15629: Add approval step before publishing in Nexus [#70](https://github.com/Jahia/sandbox/pull/70) ([@rknj](https://github.com/rknj) [@jahia-ci](https://github.com/jahia-ci))

#### ⚠️ Pushed to `main`

- Moved all jobs to orbs ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Added update-signature orb command ([@Fgerthoffert](https://github.com/Fgerthoffert))

#### Authors: 4

- Francois G. ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Jahia Continuous Integration account ([@jahia-ci](https://github.com/jahia-ci))
- Michael De Checchi ([@rknj](https://github.com/rknj))
- Thomas Draier ([@tdraier](https://github.com/tdraier))

---

# v1.6.1 (Fri Apr 09 2021)

#### 🐛 Bug Fix

- Add option to automatically drop the staging repo after release ([@rknj](https://github.com/rknj))
- Update readme ([@rknj](https://github.com/rknj))

#### Authors: 1

- Michael De Checchi ([@rknj](https://github.com/rknj))

---

# v1.5.3 (Thu Apr 08 2021)

#### 🐛 Bug Fix

- Add logs for Nexus actions ([@rknj](https://github.com/rknj))
- Update the queries to prevent a bad request error ([@rknj](https://github.com/rknj))
- Add missing curly brackets ([@rknj](https://github.com/rknj))
- Group the steps to ease the usage of variables ([@rknj](https://github.com/rknj))
- Update the release jobs for publication and cancellation + fix typos ([@rknj](https://github.com/rknj))
- Add the drop job and fix typo ([@rknj](https://github.com/rknj))
- Add steps to retrieve the staging repository ID and promote it ([@rknj](https://github.com/rknj))
- BACKLOG-15629: Add a cancel job step in rollback job and approval steps in the on-release workflow ([@rknj](https://github.com/rknj))
- BACKLOG-15719: add auto to yarn as it is done in generate log in case… [#69](https://github.com/Jahia/sandbox/pull/69) ([@cedmail](https://github.com/cedmail))
- Add rollback workflow and github trigger; Modified README.md [#68](https://github.com/Jahia/sandbox/pull/68) ([@gflores-jahia](https://github.com/gflores-jahia))
- Removed a volume bind not necessary anymore and updated Cypress to 6.6.0 [#64](https://github.com/Jahia/sandbox/pull/64) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Add mocha junit reporter into cypress to have test results into tests… [#62](https://github.com/Jahia/sandbox/pull/62) ([@cedmail](https://github.com/cedmail))
- Mention node version for cypress config [#63](https://github.com/Jahia/sandbox/pull/63) ([@AKarmanov](https://github.com/AKarmanov))
- Cleaned up container name [#61](https://github.com/Jahia/sandbox/pull/61) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Resized the viewport for Cypress [#59](https://github.com/Jahia/sandbox/pull/59) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Replaced jahia-cli with built-in Jahia provisioning [#58](https://github.com/Jahia/sandbox/pull/58) ([@Fgerthoffert](https://github.com/Fgerthoffert))

#### ⚠️ Pushed to `main`

- Manual restore sandbox version signature ([@gflores-jahia](https://github.com/gflores-jahia))
- Manual restore sandbox version to latest dev version ([@gflores-jahia](https://github.com/gflores-jahia))
- [ci skip] Updated signature for Sandbox Module ([@jahia-ci](https://github.com/jahia-ci))
- Revert "WIP SAVEPOINT" ([@gflores-jahia](https://github.com/gflores-jahia))
- WIP SAVEPOINT ([@gflores-jahia](https://github.com/gflores-jahia))

#### Authors: 6

- Alex Karmanov ([@AKarmanov](https://github.com/AKarmanov))
- Cedric Mailleux ([@cedmail](https://github.com/cedmail))
- Francois G. ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Geofrey Flores ([@gflores-jahia](https://github.com/gflores-jahia))
- Jahia Continuous Integration account ([@jahia-ci](https://github.com/jahia-ci))
- Michael De Checchi ([@rknj](https://github.com/rknj))

---

# v1.5.3 (Thu Apr 08 2021)

#### 🐛 Bug Fix

- Update the queries to prevent a bad request error ([@rknj](https://github.com/rknj))
- Add missing curly brackets ([@rknj](https://github.com/rknj))
- Group the steps to ease the usage of variables ([@rknj](https://github.com/rknj))
- Update the release jobs for publication and cancellation + fix typos ([@rknj](https://github.com/rknj))
- Add the drop job and fix typo ([@rknj](https://github.com/rknj))
- Add steps to retrieve the staging repository ID and promote it ([@rknj](https://github.com/rknj))
- BACKLOG-15629: Add a cancel job step in rollback job and approval steps in the on-release workflow ([@rknj](https://github.com/rknj))
- BACKLOG-15719: add auto to yarn as it is done in generate log in case… [#69](https://github.com/Jahia/sandbox/pull/69) ([@cedmail](https://github.com/cedmail))
- Add rollback workflow and github trigger; Modified README.md [#68](https://github.com/Jahia/sandbox/pull/68) ([@gflores-jahia](https://github.com/gflores-jahia))
- Removed a volume bind not necessary anymore and updated Cypress to 6.6.0 [#64](https://github.com/Jahia/sandbox/pull/64) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Add mocha junit reporter into cypress to have test results into tests… [#62](https://github.com/Jahia/sandbox/pull/62) ([@cedmail](https://github.com/cedmail))
- Mention node version for cypress config [#63](https://github.com/Jahia/sandbox/pull/63) ([@AKarmanov](https://github.com/AKarmanov))
- Cleaned up container name [#61](https://github.com/Jahia/sandbox/pull/61) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Resized the viewport for Cypress [#59](https://github.com/Jahia/sandbox/pull/59) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Replaced jahia-cli with built-in Jahia provisioning [#58](https://github.com/Jahia/sandbox/pull/58) ([@Fgerthoffert](https://github.com/Fgerthoffert))

#### ⚠️ Pushed to `main`

- Manual restore sandbox version signature ([@gflores-jahia](https://github.com/gflores-jahia))
- Manual restore sandbox version to latest dev version ([@gflores-jahia](https://github.com/gflores-jahia))
- [ci skip] Updated signature for Sandbox Module ([@jahia-ci](https://github.com/jahia-ci))
- Revert "WIP SAVEPOINT" ([@gflores-jahia](https://github.com/gflores-jahia))
- WIP SAVEPOINT ([@gflores-jahia](https://github.com/gflores-jahia))

#### Authors: 6

- Alex Karmanov ([@AKarmanov](https://github.com/AKarmanov))
- Cedric Mailleux ([@cedmail](https://github.com/cedmail))
- Francois G. ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Geofrey Flores ([@gflores-jahia](https://github.com/gflores-jahia))
- Jahia Continuous Integration account ([@jahia-ci](https://github.com/jahia-ci))
- Michael De Checchi ([@rknj](https://github.com/rknj))

---

# v1.5.3 (Wed Apr 07 2021)

#### 🐛 Bug Fix

- Add missing curly brackets ([@rknj](https://github.com/rknj))
- Group the steps to ease the usage of variables ([@rknj](https://github.com/rknj))
- Update the release jobs for publication and cancellation + fix typos ([@rknj](https://github.com/rknj))
- Add the drop job and fix typo ([@rknj](https://github.com/rknj))
- Add steps to retrieve the staging repository ID and promote it ([@rknj](https://github.com/rknj))
- BACKLOG-15629: Add a cancel job step in rollback job and approval steps in the on-release workflow ([@rknj](https://github.com/rknj))
- BACKLOG-15719: add auto to yarn as it is done in generate log in case… [#69](https://github.com/Jahia/sandbox/pull/69) ([@cedmail](https://github.com/cedmail))
- Add rollback workflow and github trigger; Modified README.md [#68](https://github.com/Jahia/sandbox/pull/68) ([@gflores-jahia](https://github.com/gflores-jahia))
- Removed a volume bind not necessary anymore and updated Cypress to 6.6.0 [#64](https://github.com/Jahia/sandbox/pull/64) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Add mocha junit reporter into cypress to have test results into tests… [#62](https://github.com/Jahia/sandbox/pull/62) ([@cedmail](https://github.com/cedmail))
- Mention node version for cypress config [#63](https://github.com/Jahia/sandbox/pull/63) ([@AKarmanov](https://github.com/AKarmanov))
- Cleaned up container name [#61](https://github.com/Jahia/sandbox/pull/61) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Resized the viewport for Cypress [#59](https://github.com/Jahia/sandbox/pull/59) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Replaced jahia-cli with built-in Jahia provisioning [#58](https://github.com/Jahia/sandbox/pull/58) ([@Fgerthoffert](https://github.com/Fgerthoffert))

#### ⚠️ Pushed to `main`

- Manual restore sandbox version signature ([@gflores-jahia](https://github.com/gflores-jahia))
- Manual restore sandbox version to latest dev version ([@gflores-jahia](https://github.com/gflores-jahia))
- [ci skip] Updated signature for Sandbox Module ([@jahia-ci](https://github.com/jahia-ci))
- Revert "WIP SAVEPOINT" ([@gflores-jahia](https://github.com/gflores-jahia))
- WIP SAVEPOINT ([@gflores-jahia](https://github.com/gflores-jahia))

#### Authors: 6

- Alex Karmanov ([@AKarmanov](https://github.com/AKarmanov))
- Cedric Mailleux ([@cedmail](https://github.com/cedmail))
- Francois G. ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Geofrey Flores ([@gflores-jahia](https://github.com/gflores-jahia))
- Jahia Continuous Integration account ([@jahia-ci](https://github.com/jahia-ci))
- Michael De Checchi ([@rknj](https://github.com/rknj))

---

# v1.5.3 (Wed Apr 07 2021)

#### 🐛 Bug Fix

- Group the steps to ease the usage of variables ([@rknj](https://github.com/rknj))
- Update the release jobs for publication and cancellation + fix typos ([@rknj](https://github.com/rknj))
- Add the drop job and fix typo ([@rknj](https://github.com/rknj))
- Add steps to retrieve the staging repository ID and promote it ([@rknj](https://github.com/rknj))
- BACKLOG-15629: Add a cancel job step in rollback job and approval steps in the on-release workflow ([@rknj](https://github.com/rknj))
- BACKLOG-15719: add auto to yarn as it is done in generate log in case… [#69](https://github.com/Jahia/sandbox/pull/69) ([@cedmail](https://github.com/cedmail))
- Add rollback workflow and github trigger; Modified README.md [#68](https://github.com/Jahia/sandbox/pull/68) ([@gflores-jahia](https://github.com/gflores-jahia))
- Removed a volume bind not necessary anymore and updated Cypress to 6.6.0 [#64](https://github.com/Jahia/sandbox/pull/64) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Add mocha junit reporter into cypress to have test results into tests… [#62](https://github.com/Jahia/sandbox/pull/62) ([@cedmail](https://github.com/cedmail))
- Mention node version for cypress config [#63](https://github.com/Jahia/sandbox/pull/63) ([@AKarmanov](https://github.com/AKarmanov))
- Cleaned up container name [#61](https://github.com/Jahia/sandbox/pull/61) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Resized the viewport for Cypress [#59](https://github.com/Jahia/sandbox/pull/59) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Replaced jahia-cli with built-in Jahia provisioning [#58](https://github.com/Jahia/sandbox/pull/58) ([@Fgerthoffert](https://github.com/Fgerthoffert))

#### ⚠️ Pushed to `main`

- Manual restore sandbox version signature ([@gflores-jahia](https://github.com/gflores-jahia))
- Manual restore sandbox version to latest dev version ([@gflores-jahia](https://github.com/gflores-jahia))
- [ci skip] Updated signature for Sandbox Module ([@jahia-ci](https://github.com/jahia-ci))
- Revert "WIP SAVEPOINT" ([@gflores-jahia](https://github.com/gflores-jahia))
- WIP SAVEPOINT ([@gflores-jahia](https://github.com/gflores-jahia))

#### Authors: 6

- Alex Karmanov ([@AKarmanov](https://github.com/AKarmanov))
- Cedric Mailleux ([@cedmail](https://github.com/cedmail))
- Francois G. ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Geofrey Flores ([@gflores-jahia](https://github.com/gflores-jahia))
- Jahia Continuous Integration account ([@jahia-ci](https://github.com/jahia-ci))
- Michael De Checchi ([@rknj](https://github.com/rknj))

---

# v1.5.3 (Wed Apr 07 2021)

#### 🐛 Bug Fix

- Update the release jobs for publication and cancellation + fix typos ([@rknj](https://github.com/rknj))
- Add the drop job and fix typo ([@rknj](https://github.com/rknj))
- Add steps to retrieve the staging repository ID and promote it ([@rknj](https://github.com/rknj))
- BACKLOG-15629: Add a cancel job step in rollback job and approval steps in the on-release workflow ([@rknj](https://github.com/rknj))
- BACKLOG-15719: add auto to yarn as it is done in generate log in case… [#69](https://github.com/Jahia/sandbox/pull/69) ([@cedmail](https://github.com/cedmail))
- Add rollback workflow and github trigger; Modified README.md [#68](https://github.com/Jahia/sandbox/pull/68) ([@gflores-jahia](https://github.com/gflores-jahia))
- Removed a volume bind not necessary anymore and updated Cypress to 6.6.0 [#64](https://github.com/Jahia/sandbox/pull/64) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Add mocha junit reporter into cypress to have test results into tests… [#62](https://github.com/Jahia/sandbox/pull/62) ([@cedmail](https://github.com/cedmail))
- Mention node version for cypress config [#63](https://github.com/Jahia/sandbox/pull/63) ([@AKarmanov](https://github.com/AKarmanov))
- Cleaned up container name [#61](https://github.com/Jahia/sandbox/pull/61) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Resized the viewport for Cypress [#59](https://github.com/Jahia/sandbox/pull/59) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Replaced jahia-cli with built-in Jahia provisioning [#58](https://github.com/Jahia/sandbox/pull/58) ([@Fgerthoffert](https://github.com/Fgerthoffert))

#### ⚠️ Pushed to `main`

- Manual restore sandbox version signature ([@gflores-jahia](https://github.com/gflores-jahia))
- Manual restore sandbox version to latest dev version ([@gflores-jahia](https://github.com/gflores-jahia))
- [ci skip] Updated signature for Sandbox Module ([@jahia-ci](https://github.com/jahia-ci))
- Revert "WIP SAVEPOINT" ([@gflores-jahia](https://github.com/gflores-jahia))
- WIP SAVEPOINT ([@gflores-jahia](https://github.com/gflores-jahia))

#### Authors: 6

- Alex Karmanov ([@AKarmanov](https://github.com/AKarmanov))
- Cedric Mailleux ([@cedmail](https://github.com/cedmail))
- Francois G. ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Geofrey Flores ([@gflores-jahia](https://github.com/gflores-jahia))
- Jahia Continuous Integration account ([@jahia-ci](https://github.com/jahia-ci))
- Michael De Checchi ([@rknj](https://github.com/rknj))

---

# v1.5.3 (Thu Mar 11 2021)

#### 🐛 Bug Fix

- Removed a volume bind not necessary anymore and updated Cypress to 6.6.0 [#64](https://github.com/Jahia/sandbox/pull/64) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Add mocha junit reporter into cypress to have test results into tests… [#62](https://github.com/Jahia/sandbox/pull/62) ([@cedmail](https://github.com/cedmail))
- Mention node version for cypress config [#63](https://github.com/Jahia/sandbox/pull/63) ([@AKarmanov](https://github.com/AKarmanov))
- Cleaned up container name [#61](https://github.com/Jahia/sandbox/pull/61) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Resized the viewport for Cypress [#59](https://github.com/Jahia/sandbox/pull/59) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Replaced jahia-cli with built-in Jahia provisioning [#58](https://github.com/Jahia/sandbox/pull/58) ([@Fgerthoffert](https://github.com/Fgerthoffert))

#### ⚠️ Pushed to `main`

- Revert "WIP SAVEPOINT" ([@gflores-jahia](https://github.com/gflores-jahia))
- WIP SAVEPOINT ([@gflores-jahia](https://github.com/gflores-jahia))
- [ci skip] Updated signature for Sandbox Module ([@jahia-ci](https://github.com/jahia-ci))

#### Authors: 5

- Alex Karmanov ([@AKarmanov](https://github.com/AKarmanov))
- Cedric Mailleux ([@cedmail](https://github.com/cedmail))
- Francois G. ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Geofrey Flores ([@gflores-jahia](https://github.com/gflores-jahia))
- Jahia Continuous Integration account ([@jahia-ci](https://github.com/jahia-ci))

---

# v1.5.2 (Fri Feb 19 2021)

#### 🐛 Bug Fix

- fix next development version issue [#56](https://github.com/Jahia/sandbox/pull/56) ([@jsinovassin](https://github.com/jsinovassin))
- Updated to Apollo 3 [#54](https://github.com/Jahia/sandbox/pull/54) ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Updated cypress and added logger to file [#53](https://github.com/Jahia/sandbox/pull/53) ([@Fgerthoffert](https://github.com/Fgerthoffert))

#### ⚠️ Pushed to `main`

- [ci skip] Updated signature for Sandbox Module ([@jahia-ci](https://github.com/jahia-ci))

#### Authors: 3

- [@jsinovassin](https://github.com/jsinovassin)
- Francois G. ([@Fgerthoffert](https://github.com/Fgerthoffert))
- Jahia Continuous Integration account ([@jahia-ci](https://github.com/jahia-ci))

---

# v1.5.1 (Wed Feb 17 2021)

#### 🐛 Bug Fix

- QABACKLOG-555: Remove the v1-sonar-owasp-dependencies- key reference [#52](https://github.com/Jahia/sandbox/pull/52) ([@bpapez](https://github.com/bpapez))
- BACKLOG-15467 : improve changelog generation [#51](https://github.com/Jahia/sandbox/pull/51) ([@jsinovassin](https://github.com/jsinovassin))

#### Authors: 2

- [@jsinovassin](https://github.com/jsinovassin)
- Benjamin Papez ([@bpapez](https://github.com/bpapez))

---

@default
Please update this file with the repository's changelog.
