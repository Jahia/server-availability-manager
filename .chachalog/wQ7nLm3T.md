---
server-availability-manager: patch
---

Fixed the health check response when a probe message carries a non-ASCII character. The response was cut short, and the character itself was corrupted.
