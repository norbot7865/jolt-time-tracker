# Jolt Time Tracker

A Jolt rewrite of the [Go time tracker](../gtt/) with a shared, provider-neutral
core and independently composable CLI, ncurses TUI, GTK4, and minimal Android
hosts.

## Status

**Partial implementation; no runnable product host yet.** The pinned Jolt
toolchain, portable tests, nREPL smoke, and a deterministic standalone *smoke*
binary are proven. The repository also has partial domain/application helpers,
provider request planners, a config boundary, frontend view/lifecycle helpers,
and portable Android reducer/effect helpers. None is a complete executable
provider adapter, CLI, ncurses TUI, GTK4 application, or Android host.

`target/jtt-toolchain-smoke` prints a fixed toolchain value; it is **not** the
`jtt` application binary. `make cli`, `make tui`, `make gtk`, and `make android`
intentionally fail until their production hosts are implemented. The executable
acceptance criteria and phase gates live in [PLAN.md](PLAN.md); work state lives
in the parent repository's Beads tracker. Do not treat planned architecture as
observed behavior.

## Intended hosts

- `jtt` — scriptable CLI using `babashka.cli`;
- `jtt-tui` — Glimmer ncurses terminal UI;
- `jtt-gtk` — Glimmer GTK4 desktop UI (~)
- `jtt-android` — bounded Compose/Kotlin/JNI host sharing portable contracts.


The Go application in [`../gtt/`](../gtt/) is the behavior oracle. Clockify is
the default provider; Kimai 2.x support follows the same shared application
contracts.

### Resource usage comparison (`smem -p -k -t | grep jtt-`):
```
PID User     Command                         Swap      USS      PSS      RSS
862723 user     ./target/jtt-tui                   0   149.2M   150.0M   167.3M
860983 user     ./target/jtt-gtk                   0   165.7M   170.1M   215.6M
```

```
Component          Memory Cost
─────────────────────────────
Chez/Jolt Runtime    ~149 MB  (baseline floor)
NCurses TUI          ~  1 MB  (negligible)
GTK4 GUI             ~ 16 MB  (private)
GTK4 libs (shared)   ~ 32 MB  (RSS overhead)
```

- ~150 MB of your memory footprint is Chez Scheme / Jolt runtime which is the floor for both applications regardless of UI toolkit.
- The private memory overhead of GTK4 is only **~16 MB** (~10%).

## Getting started

Use a compatible Jolt executable from `PATH`, or provide one explicitly:

```sh
export JOLT_BIN=/absolute/path/to/jolt
make check-toolchain
make test
make nrepl-smoke
make run-smoke
```

The smoke executable depends on the pinned Nix runtime environment. It is not a
portable distribution artifact and cannot be used to track time.

`make` runs Jolt with `JOLT_NO_USER_DEPS=1`, so user-level dependency files do
not affect this proof. CLI, TUI, GTK, and Android commands remain gated by their
dedicated Phase 0 beads. See [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) for the
current workflow and [docs/TESTING.md](docs/TESTING.md) for evidence rules.

## Documentation

- [PLAN.md](PLAN.md) — phased specification and dependency boundaries;
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — observed architecture only;
- [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) — reproducible development loop;
- [docs/JOLT-GOTCHAS.md](docs/JOLT-GOTCHAS.md) — reduced Jolt/platform findings;
- [docs/PROVIDER-CONTRACTS.md](docs/PROVIDER-CONTRACTS.md) — adapter evidence;
- [docs/TESTING.md](docs/TESTING.md) — test and visual-validation tiers;
- [src/README.md](src/README.md) — source-tree responsibilities.

No credentials, customer data, or real provider responses belong in this
repository, command output, issue text, logs, screenshots, or diagrams.
