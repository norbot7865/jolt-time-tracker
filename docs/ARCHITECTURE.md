# Observed architecture

## Status

**Bootstrap state — no runtime architecture has been observed.** The intended
ports-and-adapters design is specified in [PLAN.md](../PLAN.md), but this file
will describe only behavior demonstrated by tests or reproducible experiments.

## Bootstrap boundary

The repository currently contains documentation and a structural verifier only.
It deliberately has no Jolt dependency file, source namespace, provider
adapter, UI toolkit, Android runtime, or executable product command. The next
Phase 0 beads must prove their own runtime boundaries before this document can
claim them.

```d2
direction: right

human: "Developer or agent"
repo: "jtt repository\nbootstrap docs + layout"
verify: "scripts/verify-bootstrap\nstructure + diff check"
future: "Unproven Phase 0 gates\nJolt / CLI / Glimmer / Android"

human -> repo: "reads contracts"
repo -> verify: "make verify-bootstrap"
verify -> future: "does not validate or imply"
```

## Evidence

`make verify-bootstrap` checks required documentation and every planned
`src/jtt/**` responsibility directory for a README, then runs `git diff --check`.
It does not prove Jolt behavior.
