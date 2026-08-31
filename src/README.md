# Source architecture

## Status

This directory is a documented scaffold with one Phase 0 portable toolchain
smoke namespace in `jtt.bootstrap.smoke`. It proves only source loading, testing,
nREPL evaluation, and standalone compilation; no Phase 1/2 domain or application
contract is implemented yet. The layout protects the intended dependency
direction before that code arrives.

```d2
direction: down

hosts: "CLI / TUI / GTK / Android hosts"
frontend: "frontend\ninteraction state + presentation"
application: "application\nprovider-neutral workflows"
ports: "port\nfocused capabilities and contracts"
domain: "domain\npure values, validation, time rules"
adapters: "adapter\nprovider, HTTP, config translation"
bootstrap: "bootstrap\nindependent composition roots"

hosts -> frontend
frontend -> application
application -> ports
application -> domain
adapters -> ports
bootstrap -> frontend
bootstrap -> adapters
```

- `domain/` remains pure and UI/provider/filesystem/JNI-free.
- `port/` declares data contracts and focused capabilities without construction.
- `application/` coordinates domain rules through injected capabilities.
- `adapter/` owns external DTOs, transport, configuration, and normalization.
- `frontend/` owns host interaction, never importing another frontend.
- `android/` holds portable reducer/effect/wire contracts only.
- `bootstrap/` composes each executable host independently.

Update this file and the affected child README whenever these responsibilities
or dependency directions change.
