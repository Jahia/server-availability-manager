---
server-availability-manager: patch
---

Fixed two health check probe configuration defects. A `DBConnectivity` timeout made the probe reject its configuration, which left every probe after it in the list with the configuration it had before. The `ServerLoad` thread load red threshold was also spelled `theadLoadRedThreshold`, which is now `threadLoadRedThreshold`. An instance that set the old spelling must rename that property, because the old one is no longer read.
