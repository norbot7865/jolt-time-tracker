# Development workflow

## Current bootstrap commands

```sh
make help
make verify-bootstrap
```

`verify-bootstrap` is intentionally independent of Jolt. It validates the
initial layout and documentation; it is not a product test.

## Required loop after the toolchain gate

1. Read [AGENTS.md](../AGENTS.md), the relevant source README, and any ADR.
2. Start or attach the pinned Jolt nREPL.
3. Evaluate the smallest explicit, secret-free input.
4. Inspect the result, encode it in a focused test, and remove probes.
5. Run targeted tests and the applicable binary/UI/platform smoke.
6. Update observed documentation and exact Beads evidence.
7. Review `git diff --check`, commit one focused bead, and push the intended
   source and Beads/Dolt repositories.

The planned commands (`jolt nrepl-server`, `jolt -M:test`, `jolt test-core`,
and frontend build commands) are not available until their owning Phase 0
beads pin and prove them. Never substitute JVM Clojure for Jolt evidence.

## Visual and platform evidence

Use Wayland first. A controlled fallback is test harness evidence only. UI work
requires semantic state assertions plus synthetic-data screenshots and a
vision-capable review. Android evidence must label native execution separately
from ARM64 translation on an x86_64 emulator.
