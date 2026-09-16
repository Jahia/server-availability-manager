# server-availability-manager Changelog

## 0.0.1

* Fixed the Health check endpoint authorization to work reliably under all security profiles.

* A configuration change now updates the probes registry and the health check servlet in place. Before, the change restarted both, so while the restart was in progress the health check could report no probes and the `/healthcheck` endpoint could answer 404.

* Restricted caching of the server health check response so each call is answered by the server, not from a cache.
