# server-availability-manager Changelog

## 0.1.0

### New Features

* Added a health check probe that reports a module whose internal components failed to start. A module can be reported as started while a component inside it never ran, and no probe covered that state before.

### Bug Fixes

* Fixed the Health check endpoint authorization to work reliably under all security profiles.

* A configuration change now updates the probes registry and the health check servlet in place. Before, the change restarted both, so while the restart was in progress the health check could report no probes and the `/healthcheck` endpoint could answer 404.

* Restricted caching of the server health check response so each call is answered by the server, not from a cache.

* Fixed the health check response when a probe message carries a non-ASCII character. The response was cut short, and the character itself was corrupted.
