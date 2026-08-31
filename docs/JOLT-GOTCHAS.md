# Jolt and platform gotchas

## Status

Add only reduced, versioned observations from a Phase 0 experiment. Do not turn
assumptions into facts.

## Observed — standalone build prerequisites (Fedora 44)

Jolt `v0.7.28-45-g447b874d` reached native linking only after `xxd` and the
ncurses development linker inputs were available. Missing `xxd` stopped the
build while generating `boot_data.h`; missing ncurses development libraries
stopped the link at `-lncurses -ltinfo`. The project flake supplies `xxd` and
`ncurses` along with the required compiler/runtime inputs, so validation should
use `nix develop` rather than rely on mutable host packages. The initial raw
source build exceeded a five-minute harness limit during Chez compilation; it
is a performance observation, not a failed compilation claim.

## Baseline constraints requiring experiments

- Jolt is a Clojure implementation on Scheme, not a JVM runtime.
- Java interop, JVM lifecycle, UTF-16 string indexing, Java regex behavior, and
  Java filesystem/time assumptions do not transfer automatically.
- Every library, host API, native dependency, and platform claim must be pinned
  and tested on the actual target.
- A failed feasibility experiment is evidence, not permission to silently use a
  different parser, toolkit, runtime, or platform.

Record each finding with: pinned revision, host/environment, smallest command
or fixture, observed output/error, impact, and safe workaround or blocking
Beads ID. Never include credentials or real provider payloads.
