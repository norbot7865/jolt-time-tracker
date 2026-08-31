# Jolt Time Tracker

A Jolt rewrite of the [Go time tracker](../gtt/) with a shared, provider-neutral
core and independently composable CLI, ncurses TUI, GTK4, and minimal Android
hosts.

## Status

**Bootstrap only.** No Jolt runtime, providers, or user-facing commands are
implemented yet. The executable acceptance criteria and phase gates live in
[PLAN.md](PLAN.md); work state lives in the parent repository's Beads tracker.
Do not treat planned architecture as observed behavior.

## Intended hosts

- `jtt` — scriptable CLI using `babashka.cli`;
- `jtt-tui` — Glimmer ncurses terminal UI;
- `jtt-gtk` — Glimmer GTK4 desktop UI;
- `jtt-android` — bounded Compose/Kotlin/JNI host sharing portable contracts.

The Go application in [`../gtt/`](../gtt/) is the behavior oracle. Clockify is
the default provider; Kimai 2.x support follows the same shared application
contracts.

## Getting started

The initial commands are intentionally safe and fail fast without a Jolt
installation:

```sh
make help
make verify-bootstrap
```

Toolchain, REPL, test, binary, and frontend commands are introduced only after
the feasibility gates have pinned and proven them. See
[docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) for the current workflow and
[docs/TESTING.md](docs/TESTING.md) for evidence rules.

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
