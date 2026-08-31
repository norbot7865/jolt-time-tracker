# Testing and evidence

## Current observed gates

`make verify-bootstrap` checks the documentation/layout contract and
whitespace-safe diff. `make test`, `make nrepl-smoke`, and `make run-smoke`
exercise portable helpers, the loopback nREPL smoke, and the fixed-output
smoke namespace respectively. None proves a runnable time-tracker binary or a
native desktop/mobile host.

`make verify-bootstrap` is not a substitute for Jolt tests.

## Planned evidence tiers

1. Pure domain and reducer tests with injected clock/effects.
2. Application and adapter contract tests using synthetic local servers/files.
3. Built-process CLI tests covering stdout, stderr, JSON, and exit status.
4. TUI buffer-screen tests plus a real pseudo-terminal smoke.
5. GTK widget/state tests, live-reload smoke, safe screenshot, and vision review.
6. Android instrumentation, owner-thread/lifecycle/stress checks, semantic
   Compose assertions, and clearly labeled native or translated evidence.
7. Fresh-clone fail-fast verification across supported tiers.

Tests must fail nonzero on failed assertions. Skips must state the missing
platform prerequisite. Fixtures, captures, diagrams, and error output use
synthetic data and redact secrets.
