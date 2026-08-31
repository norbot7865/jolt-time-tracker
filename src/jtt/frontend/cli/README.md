# CLI frontend

The Phase 0 compatibility smoke proves the pinned `babashka.cli` library loads
on Jolt, parses strict Unix options, and returns process errors for unknown
options. `jtt.frontend.cli.core` additionally exposes a partial injected-session
command dispatcher, but it has no process entry point, stream handling, output
renderer, exit mapping, or complete command semantics. It is not the product
CLI. Future work adds these boundaries without loading GUI or ncurses
dependencies.
