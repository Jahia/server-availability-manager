---
server-availability-manager: minor
---

Added a health check probe that reports a module whose internal components failed to start. A module can be reported as started while a component inside it never ran, and no probe covered that state before.
