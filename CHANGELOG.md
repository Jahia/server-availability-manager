# server-availability-manager Changelog

## 0.0.1

* Fixed the Health check endpoint authorization to work reliably under all security profiles.

* Restricted caching of the server health check response so each call is answered by the server, not from a cache.
