# Development workflow

## Pinned Phase 0 toolchain

The current proof ran on Fedora 44 with Jolt
`v0.7.28-45-g447b874d`, source revision
`447b874d06066d15fee187200fabaf410f4ff5b6`. `deps.edn` records a minimum Jolt
version of `0.7.28` and the full observed source revision. This is a source
runtime gate, not a vendored Jolt dependency.

Use the pinned Nix development shell. It provides Chez, Jolt's pinned source,
C compiler/link requirements, `xxd`, and test tooling without relying on mutable
host packages:

```sh
nix --extra-experimental-features 'nix-command flakes' develop -c make check-toolchain
nix --extra-experimental-features 'nix-command flakes' develop -c make test
nix --extra-experimental-features 'nix-command flakes' develop -c make nrepl-smoke
nix --extra-experimental-features 'nix-command flakes' develop -c make run-smoke
```

The shell exports `JOLT_BIN` from the revision pinned by `flake.lock`. A
compatible installed executable is also supported for a focused investigation:
`export JOLT_BIN=/absolute/path/to/jolt`. All Makefile Jolt invocations set
`JOLT_NO_USER_DEPS=1`. This intentionally prevents
`$XDG_CONFIG_HOME/clojure/deps.edn` or `~/.clojure/deps.edn` from changing the
project proof. `scripts/jolt` fails with exit 127 if neither `JOLT_BIN` nor a
`jolt` executable on `PATH` is available.

Observed commands and results:

```text
JOLT_NO_USER_DEPS=1 jolt -e '(str "jtt-toolchain:" (+ 40 2))'
=> "jtt-toolchain:42"

make test
=> portable suite passes (consult the test runner output for its current count)

make nrepl-smoke
=> nREPL smoke passed: jtt-toolchain:42

make run-smoke
=> jtt-toolchain:42
```

`nrepl-smoke` starts a temporary loopback server, sends a bencoded `eval` that
loads `jtt.bootstrap.smoke`, checks the response and shuts it down. It is an
automated counterpart to the captured live nREPL experiment; it does not expose
a listener outside loopback. `make run-smoke` builds the same fixed-output
smoke namespace; it does not build or run the `jtt` time-tracker CLI.

## Required development loop

1. Read [AGENTS.md](../AGENTS.md), the relevant source README, and any ADR.
2. Start or attach the pinned Jolt nREPL.
3. Evaluate the smallest explicit, secret-free input.
4. Inspect the result, encode it in a focused test, and remove probes.
5. Run targeted tests and the applicable binary/UI/platform smoke.
6. Update observed documentation and exact Beads evidence.
7. Review `git diff --check`, commit one focused bead, and push the intended
   source and Beads/Dolt repositories.

Never substitute JVM Clojure for Jolt evidence. Jolt's nREPL startup can take
several seconds on this source checkout; wait for its loopback port rather than
assuming a fixed short startup delay.

## Visual and platform evidence

Use Wayland first. A controlled fallback is test harness evidence only. UI work
requires semantic state assertions plus synthetic-data screenshots and a
vision-capable review. Android evidence must label native execution separately
from ARM64 translation on an x86_64 emulator.
