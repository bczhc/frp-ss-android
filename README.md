frp-ss
---
---

This works like this:

First execute `ssserver` to start a proxy server,
then start `frpc` to expose that local proxy port on a remote
server which has a public network.
After doing this, now this device becomes a proxy
server, with its proxy port exposed on a remote server,
and other devices can proxy through this device's network.

Now this only works on `aarch64-linux-android` target, since
its bundled binaries are in this architecture.
