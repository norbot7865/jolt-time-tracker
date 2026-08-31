# CLI frontend

The Phase 0 compatibility smoke proves the pinned `babashka.cli` library loads
on Jolt, parses strict Unix options, and returns process errors for unknown
options. It is not the product CLI. Future work adds stream-injected command
execution, human/JSON presentation, and complete process-exit mapping without
loading GUI or ncurses dependencies.
