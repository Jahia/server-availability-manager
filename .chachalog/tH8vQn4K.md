---
server-availability-manager: patch
---

Fixed the configuration of the health check probes.

A `DBConnectivity` timeout property made that probe throw, because it read the name of the property instead of its value. The probes are configured one after another, so every probe after that one kept the configuration it had. A probe that rejects its configuration now leaves the others untouched, and the health check logs which probe it was.

Removing a probe property now returns that setting to its default, where the probe used to keep the last value the operator set. A value that is not a whole number no longer leaves a probe half configured, and `DBConnectivity` refuses a negative timeout.

The `ServerLoad` thread load red threshold was spelled `theadLoadRedThreshold`, and it is now `threadLoadRedThreshold`. An instance that set the old spelling must rename that property, because the old one is no longer read.
