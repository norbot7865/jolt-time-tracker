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

## Observed — babashka.cli v0.9.68 compatibility

The pinned upstream source `33b1de1dfd186a9a45cfc1c4be41fe786a93dbc7`
(tag `v0.9.68`) loads and executes on pinned Jolt. The focused corpus proves
aliases, booleans, scalar/collection/custom coercion, positional arguments,
dispatch, restriction/validation errors, and `format-opts`. `format-opts` is a
pure table formatter in this revision: it does not query terminal width, so it
is safe without a terminal but terminal-width policy remains a product-CLI
concern. Dispatch returns `:args nil` when no residual arguments exist; tests
record that observed shape rather than normalizing it. No alternative parser or
JVM fallback is used.

## Observed — HTTP, JSON, time, and config primitives

On Fedora Linux with Jolt `v0.7.28-45-g447b874d`, `jolt-lang/http-client`
revision `04825632ed96a77a1c6ba1921c0a31280a3daade` loads after the flake's
OpenSSL/zlib library path is active. `org.clojure/data.json` `2.5.0` loads and
maps a synthetic JSON response. The focused local-only fixture uses
`127.0.0.1:18080`; the client test passes connect/read bounds of 1000 ms and
never logs a token or response secret. The HTTP library's total-response bound
is exposed by `jolt.http.platform/set-max-response-ms!`; this remains an
adapter policy to apply when the provider adapter is implemented.

The time fixture crosses midnight at a fixed `-05:00` offset using ASCII-only
inputs and observed `2024-01-01` then `2024-01-02`. A synthetic config is written
to a same-directory temporary path, replaced with `ATOMIC_MOVE` and
`REPLACE_EXISTING`, and observed as `rw-------` on Fedora. This is an observed
Linux proof; non-POSIX hosts need a separate permission strategy. Temporary
paths and synthetic secrets are not retained.

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
