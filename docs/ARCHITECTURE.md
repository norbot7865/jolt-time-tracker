# Observed architecture

## Status

**Phase 0 toolchain boundary observed.** The intended ports-and-adapters design
is specified in [PLAN.md](../PLAN.md), but this document describes only behavior
demonstrated by tests or reproducible experiments.

## Proven smoke boundary

The project now contains one portable namespace and a separate test namespace.
A clean Jolt invocation resolves the project source path, a live loopback nREPL
loads and evaluates the namespace, and `jolt build` creates a standalone binary
that prints the same deterministic value. No provider, HTTP, configuration, UI,
or Android boundary has been observed or implemented.

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

With `JOLT_NO_USER_DEPS=1`, `make test` runs one portable assertion,
`make nrepl-smoke` verifies an actual loopback nREPL response, and
`make run-smoke` builds and executes the standalone binary. See
[DEVELOPMENT.md](DEVELOPMENT.md) for the pinned revision and exact commands.

`make verify-bootstrap` still checks required documentation and every planned
`src/jtt/**` responsibility directory for a README, then runs `git diff --check`.
