---
server-availability-manager: patch
---

A configuration change now updates the probes registry and the health check servlet in place. Before, the change restarted both, so while the restart was in progress the health check could report no probes and the `/healthcheck` endpoint could answer 404.
