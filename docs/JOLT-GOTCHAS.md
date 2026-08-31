# Jolt and platform gotchas

## Status

No project-specific runtime finding has been reproduced yet. Add only reduced,
versioned observations from a Phase 0 experiment. Do not turn assumptions into
facts.

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
