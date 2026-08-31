# Observed architecture

## Status

**Partial boundaries observed; runnable hosts are not.** The intended
ports-and-adapters design is specified in [PLAN.md](../PLAN.md). This document
describes only behavior demonstrated by tests or reproducible experiments.

Observed portable helpers include domain timer/validation functions, injected
application workflow functions, provider request planners, config primitives,
a provider selector, frontend state/view-model helpers, and Android reducer/
effect helpers. They do not compose a provider HTTP capability or an executable
CLI, TUI, GTK4, or Android application. The only native artifact is the
standalone toolchain smoke executable described below.

## Proven smoke boundary

The project now contains one portable namespace and a separate test namespace.
A clean Jolt invocation resolves the project source path, a live loopback nREPL
loads and evaluates the namespace, and `jolt build` creates a standalone binary
that prints the same deterministic value. The executable has no time-tracker
commands and is not a distributable product artifact. Native provider HTTP,
configuration composition, ncurses, GTK4, and Android runtime boundaries remain
unimplemented.

```d2
direction: right

source: "jtt.bootstrap.smoke\nportable deterministic function"
test: "jtt.bootstrap.smoke-test\nclojure.test assertion"
nrepl: "loopback Jolt nREPL\nbencoded eval"
binary: "standalone binary\njolt build"
output: "jtt-toolchain:42"

source -> test: "required by"
source -> nrepl: "loaded and evaluated"
source -> binary: "compiled as -main"
test -> output: "asserts"
nrepl -> output: "returns"
binary -> output: "prints"
```

## Evidence

With `JOLT_NO_USER_DEPS=1`, `make test` runs the portable suite,
`make nrepl-smoke` verifies an actual loopback nREPL response, and
`make run-smoke` builds and executes the toolchain smoke binary. See
[DEVELOPMENT.md](DEVELOPMENT.md) for the pinned revision and exact commands.

`make verify-bootstrap` still checks required documentation and every planned
`src/jtt/**` responsibility directory for a README, then runs `git diff --check`.
