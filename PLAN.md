# Jolt Time Tracker (`jtt`) implementation plan

## 1. Purpose and status

This document specifies a Jolt-based rewrite/variant of the Go application in
[`../gtt/`](../gtt/). The product will preserve the provider-neutral time-tracker
semantics of `gtt` while replacing Go and its frontend frameworks with Jolt and
four separately composable hosts:

- `jtt`: scriptable command-line interface using
  [`babashka.cli`](https://github.com/babashka/cli);
- `jtt-tui`: terminal interface using Glimmer's ncurses backend, following the
  [`glimmer-tui-example`](https://github.com/jolt-lang/examples/tree/main/glimmer-tui-example);
- `jtt-gtk`: GTK4 interface using
  [`glimmer-gtk`](https://github.com/jolt-lang/glimmer-gtk), following the
  [`glimmer-app`](https://github.com/jolt-lang/examples/tree/main/glimmer-app)
  and the [Glimmer UI article](https://yogthos.net/posts/2026-08-29-glimmer-ui.html);
- `jtt-android`: a deliberately minimal Android host based on the demonstrated
  constraints and techniques in [`../../jolt/jolt-android/`](../../jolt/jolt-android/).

The CLI, TUI, and GTK application are optional frontends over one shared core.
The Android host reuses the portable domain, reducer, validation, view-model,
and wire contracts, but has a platform-specific runtime/effect boundary.

This is a specification and phase map, not the live task tracker. Beads in the
parent `time-tracker-factory` repository owns work state, dependencies,
blockers, discoveries, and validation evidence.

## 2. Product baseline and scope

### 2.1 Behavioral baseline

The Go application is the initial behavioral oracle. The Jolt variant should
cover its currently implemented capabilities:

- providers: Clockify by default and Kimai 2.x via bearer token;
- configuration load, redacted display, discovery, and secure save;
- current-user and workspace/scope discovery;
- projects and tasks/activities;
- active timer, recent entries, elapsed time, and local-day total;
- start, stop, update running entry, continue completed entry, and delete;
- description, project, task/activity, billable state, and tags;
- consistent partial-failure behavior when `continue` stops one entry but fails
  to start its replacement;
- bounded requests, contextual errors, and stale-response protection in
  interactive hosts.

The initial CLI command vocabulary should remain familiar:

```text
configure  discover  config  status
start      stop      update  continue
list       projects  tasks   delete
```

Human text output need not be byte-identical to Go, but command semantics,
structured output, validation, redaction, and exit status should be covered by
portable fixtures. JSON field names should remain compatible where practical.

### 2.2 Explicit first-release exclusions

The first release does not require:

- completed-entry create/edit forms;
- interactive settings in the TUI or GTK frontend;
- exact all-day reporting beyond the loaded provider page;
- week/month/project reporting;
- package-manager distribution or app-store publication;
- iOS;
- general Java interop in Jolt;
- a full Android UI equivalent to the desktop frontends;
- Android nREPL/CIDER parity beyond a bounded debug workflow proven safe by an
  experiment.

These exclusions may become Beads later; they must not silently broaden an
implementation bead.

## 3. Governing principles

1. **Dependencies point inward.** Domain code knows no provider, HTTP client,
   persistence format, UI toolkit, JNI object, or process-global environment.
2. **Core behavior is data-oriented.** Internal values use keyword-keyed maps,
   canonical namespaced keywords for closed vocabularies, and immutable vectors
   when stable ordering matters.
3. **Ports describe capabilities.** Application workflows receive focused
   operation maps or protocols. They do not import concrete HTTP/config/UI
   adapters.
4. **Frontends do not import one another.** They may share presenters,
   formatting, state transitions, and a deliberately portable Glimmer widget
   subset, but each has its own composition root.
5. **Jolt is not a JVM.** Current Jolt source, tests, known divergences, and
   observed behavior override JVM assumptions.
6. **REPL first, binary later.** Prove a behavior interactively, turn the
   observation into a focused test, remove probes, and only then build a
   standalone artifact.
7. **Platform claims require evidence.** Proposed, observed, inferred, and
   blocked behavior must be distinguished in documentation.
8. **Small commits are the integration unit.** One completed engineering bead
   should normally yield one focused source commit, associated Beads evidence,
   and a push.
9. **Secrets are boundary data.** Never include tokens in fixtures, logs, issue
   text, screenshots, diagrams, or exception bodies.

## 4. Target architecture

### 4.1 System view

```d2
direction: right

user: "Time-tracker user" { shape: person }

hosts: {
  label: "Optional driving hosts"
  cli: "jtt CLI\nbabashka.cli"
  tui: "jtt-tui\nGlimmer + ncursesw"
  gtk: "jtt-gtk\nGlimmer + GTK4"
  android: "jtt-android\nCompose + Kotlin/JNI"
}

core: {
  label: "Portable/shared Jolt core"
  presentation: "Events + view models\nformatting + UI-independent state"
  application: "Time-tracker use cases\nworkflow policy"
  domain: "Domain values\nvalidation + timer math"
  ports: "Provider, config, clock,\nHTTP/effect capability contracts"

  presentation -> application
  application -> domain
  application -> ports
}

adapters: {
  label: "Driven adapters"
  clockify: "Clockify adapter"
  kimai: "Kimai adapter"
  config: "XDG config adapter"
  native_http: "Jolt HTTP adapter"
  android_effects: "Android effect adapters\nHTTPS, storage, lifecycle"
}

providers: {
  clockify: "Clockify API"
  kimai: "Kimai API"
}

user -> hosts
hosts.cli -> core.presentation
hosts.tui -> core.presentation
hosts.gtk -> core.presentation
hosts.android -> core.presentation: "canonical EDN/JNI"
core.ports -> adapters
adapters.clockify -> providers.clockify
adapters.kimai -> providers.kimai
adapters.native_http -> providers
adapters.android_effects -> providers
```

### 4.2 Dependency rules

The intended source dependency direction is:

```text
entry points / composition roots
              ↓
frontend or platform adapter
              ↓
application → ports/contracts → domain
      ↓                         ↑
provider/config adapters ───────┘
```

More precisely:

- `jtt.domain.*` is pure and may depend only on portable Clojure/Jolt code.
- `jtt.application.*` depends on domain and passed capabilities.
- `jtt.port.*` contains data contracts and focused capability definitions; it
  does not instantiate implementations.
- `jtt.adapter.clockify.*`, `jtt.adapter.kimai.*`, and
  `jtt.adapter.config.*` implement the ports.
- `jtt.frontend.shared.*` contains presentation data and functions that are
  genuinely portable; it must not require a toolkit backend.
- `jtt.frontend.cli.*`, `jtt.frontend.tui.*`, and `jtt.frontend.gtk.*` depend on
  the application-facing API and their own libraries only.
- `jtt.android.*` portable namespaces contain reducer/wire behavior; Kotlin,
  JNI, Android SDK, and Compose code live under `android/`.
- only bootstrap namespaces select concrete providers, config storage, clocks,
  HTTP execution, or frontend backends.

An automated architecture test should reject imports that violate these rules.

### 4.3 Shared state and asynchronous flow

Interactive frontends should have one authoritative application state cell and
separate, bounded ephemeral UI state. Network work never runs on the GTK or
ncurses owner loop.

```d2
shape: sequence_diagram

user: User
host: "TUI or GTK host"
coordinator: "Frontend coordinator"
app: "Application use case"
provider: "Injected provider adapter"
loop: "Toolkit owner loop"

user -> host: "start / stop / refresh event"
host -> coordinator: |clojure
{:event :timer/start
 :input {:description "Review" ...}}
|
coordinator -> coordinator: "set loading + generation token"
coordinator -> app: "run on worker"
app -> provider: "provider-neutral operation"
provider -> app: "normalized domain result or error"
app -> coordinator: "result tagged with generation"
coordinator -> coordinator: "discard stale result; update ratom"
coordinator -> loop: "Glimmer schedules repaint"
loop -> host: "render derived view model"
host -> user: "authoritative state + bounded feedback"
```

Toolkit constraints are explicit:

- Glimmer cursors and reactions are created once, not during each render.
- Keyed rows use stable entry/project/task IDs, never list indexes.
- GTK signal handlers close over stable cells/IDs, not first-render values.
- GTK mutations occur only through Glimmer's scheduled main-loop path.
- TUI timers use `glimmer-tui.core/every!`/`after!`; timer callbacks do not
  perform blocking HTTP.
- TUI tests use the in-memory screen before any real-terminal smoke test.

### 4.4 Android boundary

Android is intentionally a host around a portable Jolt state machine, not a
claim that desktop native libraries work unchanged under Bionic.

```d2
direction: down

portable: "Portable .cljc\ndomain + reducer + view model + wire model"
library: "Jolt/Chez Android arm64-v8a .so"
abi: "Bounded canonical EDN C ABI\ncaller-owned/copied strings"
runtime: "Kotlin JoltRuntime\ndedicated HandlerThread"
compose: "Minimal Compose shell"
effects: "Android adapters\nHTTPS + lifecycle + storage"

portable -> library
library -> abi
abi -> runtime
runtime -> compose: "render model"
compose -> runtime: "queue event"
runtime -> effects: "execute returned effect"
effects -> runtime: "queue result event"
```

Required invariants from the Android PoC:

- use a PIC, Bionic-linked ARM64 Jolt/Chez shared library;
- make initialization, symbol lookup, dispatch, and shutdown terminally owned by
  one `HandlerThread`;
- route UI, lifecycle, worker, and HTTP callbacks onto that owner thread;
- never retain Jolt-managed string pointers across JNI calls;
- use a bounded, canonical EDN request/response contract;
- keep failed initialization terminal so queued work cannot enter a shut-down
  runtime;
- distinguish emulator translation evidence from native ARM64-device evidence;
- keep any debug evaluator/nREPL loopback-only, debug-build-only, and absent from
  release manifests and exports.

For the minimum mobile target, Android executes generic HTTPS and durable-secret
operations as host effects. Provider request planning and response normalization
should remain portable Jolt data/functions where feasible; the Kotlin adapter
should not acquire time-tracking policy. Credentials stay in Android
Keystore-backed storage and are injected at execution time rather than stored in
portable reducer state or serialized diagnostics.

The initial Android UI is bounded to configuration status, active timer/recent
entries, refresh, start, and stop. Continue/update/delete and Kimai can follow
only after the core/runtime boundary is stable. Clockify-only mobile support is
an acceptable first milestone if clearly labeled and if the shared contracts do
not hard-code Clockify.

## 5. Proposed repository structure

`jtt` will become its own Git repository and a submodule of
`time-tracker-factory`, parallel to `gtt`, `rtt`, and `ett`.

```text
jtt/
├── AGENTS.md                    self-contained coding-agent instructions
├── README.md                    concise, human-facing product documentation
├── PLAN.md                      this specification
├── deps.edn                     pinned Jolt/Clojure/native dependencies + tasks
├── Makefile                     discoverable build/test/smoke entry points
├── .clj-kondo/config.edn        Jolt macro/namespace lint configuration
├── src/
│   ├── README.md                complete source architecture overview
│   └── jtt/
│       ├── domain/              pure models, validation, totals, timer rules
│       │   └── README.md
│       ├── port/                capability and boundary contracts
│       │   └── README.md
│       ├── application/         provider-neutral workflows
│       │   └── README.md
│       ├── adapter/
│       │   ├── README.md
│       │   ├── clockify/README.md
│       │   ├── kimai/README.md
│       │   ├── config/README.md
│       │   └── http/README.md
│       ├── frontend/
│       │   ├── README.md
│       │   ├── shared/README.md
│       │   ├── cli/README.md
│       │   ├── tui/README.md
│       │   └── gtk/README.md
│       ├── android/README.md    portable Android event/effect/wire contracts
│       └── bootstrap/README.md  independent composition roots
├── test/                        unit, contract, architecture, and fixture tests
├── test-resources/              sanitized Go-parity/golden fixtures
├── android/                     Gradle/Kotlin/JNI/Compose Android host
│   └── README.md
├── scripts/                     fail-fast reproducible workflows
└── docs/
    ├── ARCHITECTURE.md          observed architecture; not merely intent
    ├── DEVELOPMENT.md           REPL, tests, builds, visual validation
    ├── JOLT-GOTCHAS.md          project-specific Jolt/platform surprises
    ├── PROVIDER-CONTRACTS.md    provider mappings and API evidence
    ├── TESTING.md               tiers, fixtures, visual and live-provider rules
    ├── adr/                     durable decisions and superseding records
    └── changes/                 focused architecture-affecting change records
```

README placement is part of the architecture contract. When code changes a
subtree's responsibility or data flow, update that subtree's `README.md` and its
parents through `src/README.md`. Use fenced `d2` diagrams when a component,
sequence, state transition, data transformation, or native boundary is clearer
visually. Do not diagram mechanical changes.

Documentation roles:

- root `README.md`: install, configure, run, feature/status summary, screenshots,
  and links for humans;
- root `AGENTS.md`: repository rules, required reading, REPL workflow, Beads,
  validation, commits/pushes, Jolt caveats, and environment detection;
- `src/**/README.md`: current code architecture and narrow contracts;
- `docs/ARCHITECTURE.md`: only architecture demonstrated by tests/experiments;
- `docs/adr/`: durable decisions;
- `docs/changes/`: concrete implementation deltas when visualization helps;
- `docs/JOLT-GOTCHAS.md`: reduced, reproducible Jolt integration findings;
- Beads: task status, dependencies, blockers, progress, and command evidence.

## 6. Core contracts

### 6.1 Canonical domain data

The shared core should represent at least:

- `Config`: provider, server URL, API key reference/value at desktop boundary,
  user ID, workspace ID/name;
- `User`, `Workspace`, `Project`, `Task`;
- `Entry`: IDs, description, interval, project/task, billable, tags, provider
  metadata only when strictly necessary at an adapter boundary;
- `TimerInput`;
- `TrackerSnapshot`: active, completed entries, projects, today total;
- `ContinueResult`: stopped entry and newly started entry;
- structured error data with type, operation, provider, safe message, and cause.

Provider DTOs must not double as domain values. Clockify tag IDs and Kimai tag
names, synthetic Kimai scope, numeric Kimai IDs, datetime formats, and endpoint
paths stay in adapters.

### 6.2 Domain behavior

Pure tests should specify:

- active-entry detection;
- elapsed duration, including clock skew protection;
- local-day clipping for cross-midnight entries;
- provider default/normalization;
- timer-input trimming and validation;
- task-requires-project and description-length rules;
- deterministic sorting and defensive collection copying where relevant;
- partial-update merge semantics;
- rendering-ready view-model derivation without toolkit values.

Use an injected clock. Time-zone behavior must be proved using Jolt's time
implementation and documented; it must not be inferred from JVM `java.time`.

### 6.3 Application behavior

The application layer owns:

- effective configuration and provider selection;
- temporary provider creation for discovery;
- configured-session invariants;
- snapshot orchestration and normalization;
- start, stop, update, continue, and delete workflows;
- `continue` partial-result/error semantics;
- limits, sorting, error context, and safe redaction;
- effect/result sequencing needed by Android's reducer host.

Concurrency is an implementation choice to prove, not a parity requirement.
The first correct snapshot may load sequentially. Concurrent loading should be
added only with deterministic tests for timeout/error/stale-result behavior.

### 6.4 Provider and config ports

Desktop provider capabilities should be equivalent to the Go `Backend` surface:

```text
current user; workspaces/scopes; projects; tasks;
recent/get/active entry; start; stop; update; delete
```

Every HTTP call needs connect, read, total-response, and response-size bounds.
Responses and errors must not expose authorization headers. Contract tests use a
local fake server and assert method, path, query, auth, request body, response
mapping, and bounded error behavior.

The desktop config adapter preserves the Go path and migration behavior:

```text
${XDG_CONFIG_HOME:-~/.config}/gtt/config.json
fallback: ~/.config/clockify-tui/config.json
```

It should support the existing `GTT_*` and legacy `CLOCKIFY_*` environment
overrides. Save should use a same-directory atomic replacement and enforce
private directory/file permissions. Because filesystem and permission APIs are
host-specific, these claims require Fedora/macOS observations and a reduced
Jolt gotcha if behavior differs.

## 7. Frontend specifications

### 7.1 CLI (`jtt`)

Use `babashka.cli`, not an ad-hoc parser and not `tools.cli` as a silent
substitute. Before implementing all commands, run a focused compatibility gate
against the pinned Jolt version covering:

- namespace load;
- `dispatch` command routing;
- strict options and positional arguments;
- `:boolean`, integer, keyword, collection, and custom coercion;
- required values and validation errors;
- generated help;
- unknown command/option behavior;
- terminal-width fallback without JVM-only JLine assumptions.

`babashka.cli` currently uses JVM-facing APIs in its `:clj` branches. Jolt may
satisfy these through host shims, but that must be observed. If the current
release fails, reduce the incompatibility, test another pinned release, and
prefer a small upstream-compatible patch/fork over replacing the mandated
library. Record exact Jolt and `babashka.cli` revisions in
`docs/JOLT-GOTCHAS.md` and `deps.edn`.

The CLI must support strict Unix-style arguments (`:no-keyword-opts true` where
appropriate), generated command help, explicit coercions, machine-readable JSON,
human output, one safe error on stderr, and nonzero status on failure. Tests
invoke the runner as a function with passed args/streams before process tests.

### 7.2 TUI (`jtt-tui`)

Use `glimmer-tui` over ncursesw with Jolt 0.7.24 or newer. Expected first-release
features:

- provider/status header and live elapsed timer;
- description entry;
- project and task selection;
- billable toggle;
- start/stop and save-running actions;
- recent-entry selection, continue, delete with modal confirmation;
- today total, refresh, loading, and error/status feedback;
- discoverable focus/key help and usable small-terminal layout.

The backend is optional: core and CLI tests/builds must not initialize ncurses.
Most behavior is tested against `buffer-screen`; a separate pseudo-terminal
smoke covers actual ncurses initialization, input, timer, resize, and clean
shutdown. `TERM`, tty, and terminfo failures should produce a useful boundary
error where the upstream API permits it.

### 7.3 GTK4 GUI (`jtt-gtk`)

Use Glimmer's GTK4 backend rather than handwritten widget reconciliation.
Expected first-release controls mirror the TUI's tracker workflow. Use only the
upstream widget set initially; register a custom widget only after a focused
need and test.

Development follows the running-window workflow:

```clojure
(require '[jtt.frontend.gtk.core :as gtk]
         '[glimmer.core :as ui])
(gtk/start!)
;; redefine components or pure functions
(ui/reload! gtk/app)
(gtk/stop!)
```

State lives in top-level `defonce` reactive cells so ordinary component reloads
preserve the session. Direct GTK FFI from nREPL/worker threads is forbidden.

The primary desktop environment is Wayland. Automated visual tests may use a
controlled compositor/Xwayland or Xvfb only when the choice is documented as a
test-harness constraint rather than an application requirement. Each meaningful
UI milestone should preserve:

- semantic/widget-state assertions;
- an interaction transcript;
- screenshot(s) at stable states;
- a vision-capable agent review for clipping, contrast, hierarchy, labels,
  focus/error visibility, and obvious stale-state bugs.

No screenshot may contain a real API token or private customer data.

### 7.4 Android (`jtt-android`)

The minimal Compose shell should demonstrate, in order:

1. reproducible ARM64 Jolt library cross-build;
2. load/init/dispatch/shutdown on one owner thread;
3. repeated canonical EDN calls under allocation/GC pressure;
4. lifecycle event round trips and process recreation;
5. Keystore-backed configuration/credential reference;
6. one fake HTTP effect round trip;
7. Clockify status/snapshot;
8. start and stop;
9. semantic UI assertions and screenshots.

Native-device evidence is preferred. An API-35 x86_64 emulator running ARM64
through translation is valid bounded evidence but must be labeled as such.
Android release builds must contain no debug listener or network evaluator.

## 8. REPL-driven agent workflow

Every non-trivial coding bead should follow this loop:

```text
read local AGENTS + relevant README/ADR
→ claim one ready bead
→ start/attach Jolt nREPL
→ evaluate the smallest explicit input
→ inspect data/result or running UI
→ implement one increment
→ re-evaluate affected definitions
→ encode the observation in a focused test
→ update narrow architecture/gotcha docs
→ run affected gates
→ review diff
→ commit atomically
→ update Bead with exact evidence
→ pull/rebase and push
```

Recommended commands, to be made reproducible by project tasks/scripts:

```sh
jolt nrepl-server
jolt -M:test
jolt test-core
jolt test-cli
jolt test-tui
jolt test-gtk
jolt build-all
```

For pure investigations, evaluate canonical maps directly. Use `tap>` or a
bounded collector for temporary observations; do not print into an active TUI or
log secrets. Remove temporary probes and breakpoints before committing.

Visual frontend work requires a vision-capable coding agent or an explicit human
review handoff. Automated pixel comparison alone is insufficient, and vision
review does not replace semantic/state assertions.

## 9. Phased implementation

The phase boundaries below are dependency gates. Within a phase, work packages
with disjoint files may run in parallel once their prerequisite contracts are
merged.

### Phase 0 — repository, toolchain, and feasibility gates

Establish the standalone `jtt` repository, remote, parent submodule, pinned Jolt
toolchain, dependency lock/revision policy, scripts, and documentation skeleton.
Prove:

- Jolt run, nREPL, test, and standalone build on the current Fedora environment;
- pinned `babashka.cli` compatibility;
- pinned `glimmer-tui` headless load and real-terminal smoke;
- pinned `glimmer-gtk` load and minimal Wayland window/reload;
- `jolt.http-client`, JSON, and time-library basics;
- private atomic config-file write primitives;
- Android work is gated behind a separate experiment and does not block desktop.

Failures become reduced reproductions and Beads rather than hidden substitutions.

Exit evidence: a hello-domain REPL transcript, passing smoke tasks, a standalone
CLI binary, a GTK screenshot reviewed with vision, pinned full SHAs, and
self-contained `AGENTS.md`.

### Phase 1 — parity fixtures and frozen core seams

Extract sanitized behavioral fixtures from Go tests and documented flows. Define
canonical domain maps, error shapes, port capabilities, provider mappings,
frontend events, Android wire data, and architecture tests. Add initial ADRs for:

- ports/adapters and dependency direction;
- data representation and boundary normalization;
- shared core versus frontend-owned state;
- Android single-threaded EDN effect boundary;
- configuration compatibility/security.

No production provider UI work starts until these seams have executable fake
implementations.

Exit evidence: fixtures run through a fake in-memory application; architecture
rules fail on a deliberate violation; D2 architecture matches the proposed
contracts.

### Phase 2 — pure domain and application core

Implement pure domain behavior, view-model derivation, application workflows,
injected clock, provider registry, and fake capabilities. Develop through nREPL
with table-driven tests. Preserve `ContinueResult` partial failure and partial
update semantics explicitly.

Exit evidence: core tests cover the Go behavior matrix without network,
filesystem, terminal, GTK, or Android; the core can execute a full fake
configure → snapshot → start → update → stop → continue → delete scenario.

### Phase 3 — driven adapters, parallel after Phase 2 contracts

Run these streams independently:

- Clockify HTTP contract adapter;
- Kimai HTTP contract adapter;
- XDG/legacy config adapter;
- native HTTP execution, timeout, response limit, JSON, TLS/error safety;
- shared formatting/presentation helpers.

Provider streams own separate namespaces and fake-server fixtures. A serial
integration work package composes them through the registry and runs the shared
scenario against each fake provider.

Exit evidence: local contract tests prove every request and mapping; no live
credential is required; optional live-provider smoke is manual/secret-gated.

### Phase 4 — CLI vertical slice

Implement the complete `babashka.cli` command table and process entry point over
the shared application. Start with `config/status`, then reads, then mutations,
then configure/discover. Build a standalone `jtt` binary and compare sanitized
transcripts/JSON with the Go oracle.

Exit evidence: command/option/help/error tests, process exit tests, redaction,
all commands against fake providers, and standalone binary smoke.

### Phase 5 — TUI and GTK in parallel

After shared frontend event/view-model contracts are merged, two autonomous
streams can proceed without importing each other:

- TUI components, input routing, timers, headless interaction tests, ncurses
  smoke, and TUI docs;
- GTK components, scheduled async operations, live reload, widget-state tests,
  Wayland visual workflow, screenshots, and GTK docs.

Shared-core changes discovered by either stream become separate prerequisite
Beads and merge before both streams continue; do not patch the shared contract
inside one frontend bead.

A serial parity/integration work package runs the same fake-provider scenario
through CLI, TUI, and GTK, checking authoritative refresh after mutations and
partial failures.

Exit evidence: feature-complete desktop tracker workflows, headless/semantic
assertions, real backend smokes, screenshots and vision review, no toolkit deps
in core/CLI artifacts.

### Phase 6 — minimal Android host

Proceed through small experiments in the proof order from section 7.4. Reuse the
pinned build findings from `jolt-android`, but copy only reproducible project
scripts/contracts, not unverified claims. Keep Compose, Kotlin, JNI, and Android
HTTP/storage adapters outside the portable source tree.

The mobile stream can begin its build/runtime experiments after Phase 1's wire
contract and continue in parallel with desktop adapters. Product API operations
wait for Phase 2/3 provider planning contracts.

Exit evidence: reproducible debug APK, bounded runtime stress, lifecycle/process
recreation, fake-network test, Clockify status/start/stop, Compose semantics,
screenshot plus vision review, and an explicit evidence boundary report.

### Phase 7 — integration, hardening, and release evidence

Run clean-room builds and test tiers from a fresh checkout; measure startup,
memory, and binary sizes against the Go variants without making unsupported
performance claims. Validate cancellation/timeouts, terminal/GTK shutdown,
redaction, config permissions, malformed provider data, Unicode, time zones,
and offline errors. Update observed architecture and gotchas.

Build products remain independent:

```text
jtt          core + CLI only
jtt-tui      core + Glimmer + ncurses backend
jtt-gtk      core + Glimmer + GTK4 backend
jtt-android  Android host + embedded portable Jolt library
```

Exit evidence: fail-fast verification summary, reproducible artifact paths,
human-facing README with safe screenshots, complete source READMEs, no open
release-blocking Beads, pushed source commits, pushed Beads state, and pushed
parent submodule pointer.

## 10. Parallel work and integration policy

Parallel work begins only from a merged contract commit. Suggested ownership
boundaries are:

```text
core owner       domain/application/ports/fixtures
clockify owner   adapter/clockify + its tests/docs
kimai owner      adapter/kimai + its tests/docs
config owner     adapter/config + its tests/docs
CLI owner        frontend/cli + CLI entry point
TUI owner        frontend/tui + TUI entry point
GTK owner        frontend/gtk + GTK entry point
Android owner    android/ + portable jtt.android namespaces
integration      bootstrap, shared deps, root docs, release scripts
```

Agents working concurrently should use separate worktrees/branches or otherwise
avoid a shared mutable checkout. Shared files (`deps.edn`, root `README.md`,
`src/README.md`, bootstrap registry, and parent submodule pointer) belong to
explicit integration beads. Backend beads may propose dependency changes in
their progress notes, but the integration bead applies shared-file changes.

Each work package should be independently reviewable and should not mix
formatting, unrelated refactoring, generated artifacts, or another backend's
work.

## 11. Validation matrix

| Tier | Scope | Required evidence |
| --- | --- | --- |
| Static | namespace/dependency rules, clj-kondo where useful | no new actionable diagnostics |
| Pure | domain, reducer, formatting, view model | deterministic `clojure.test` suite |
| Application | fake ports, clocks, errors, workflows | full scenario and partial failures |
| Adapter | local fake HTTP/filesystem | protocol requests, mapping, limits, permissions |
| CLI | in-process runner + built process | stdout/stderr/JSON/help/exit status |
| TUI | in-memory screen | key/focus/modal/timer/layout assertions |
| TUI native | pseudo-terminal | ncurses init, resize, interaction, shutdown |
| GTK | component/state + live display | widget semantics, interaction transcript, screenshot |
| Android native boundary | JNI instrumentation/stress | owner thread, copied strings, lifecycle, GC |
| Android UI | Compose semantics + emulator/device | interactions, screenshot, evidence boundary |
| Clean room | fresh clone with pinned tools | fail-fast summary and reproducible artifacts |

A test command that executes zero tests is a failure. Pipelines must preserve the
actual test/build exit status. Live-provider tests are opt-in and must skip with
an explicit reason when credentials are absent.

## 12. Commit, push, and Beads protocol

The parent repository is the Beads authority. The `jtt` source repository owns
application commits. During active implementation:

1. pull Beads state, run `bd prime`, inspect `bd ready`, and claim one bead;
2. make and verify one narrow source change;
3. update the bead with exact commands and evidence;
4. commit the `jtt` change atomically with the bead ID in the message;
5. rebase on the current remote branch and push the source commit;
6. commit/sync Beads through its Dolt workflow and push Beads state;
7. update and push the parent submodule pointer only in designated integration
   beads, avoiding noisy pointer commits for unintegrated parallel branches;
8. close a bead only after its acceptance evidence is reproducible.

Do not claim a commit or push that did not occur. If source or Beads push is
blocked, preserve the local commit/state and record the exact failing command and
error.

## 13. Principal risks and gates

| Risk | Gate or mitigation |
| --- | --- |
| `babashka.cli` uses unsupported JVM-facing APIs | Phase 0 compatibility corpus; pin proven revision; reduce and upstream narrow fixes |
| Jolt library/API drift | full git SHAs, minimum versions, source/tests over assumptions |
| GTK/ncurses leak into CLI/core | separate entry namespaces, native deps, architecture/build tests |
| UI closure/reactive subscription bugs | stable cells, keyed IDs, headless interaction tests, reload tests |
| blocking network freezes UI | worker execution, generation tokens, toolkit-owned repaint scheduling |
| config permissions differ by host | filesystem experiment, private modes, documented platform evidence |
| provider API differences leak inward | normalized domain maps and local HTTP contract suites |
| Android runtime thread misuse | one terminal owner queue and instrumentation assertions |
| JNI pointer/serialization lifetime errors | copied bounded EDN strings and allocation/GC stress |
| ARM64 translation mistaken for native proof | evidence labels and native-device milestone |
| credentials leak through diagnostics/artifacts | redaction tests, synthetic fixtures, artifact review |
| parallel agents conflict on shared files | ownership boundaries and serial integration beads |
| documentation drifts from code | subtree README rule, observed architecture, D2 change records |

## 14. Reference material

Required starting references:

- Go behavior and architecture: [`../gtt/README.md`](../gtt/README.md) and
  [`../gtt/ARCHITECTURE.md`](../gtt/ARCHITECTURE.md)
- local Jolt guidance: [`../../jolt/AGENTS.md`](../../jolt/AGENTS.md) and
  `../../jolt/jolt/llms.txt`
- Jolt documentation index: <https://jolt-lang.net/llms.txt>
- Jolt differences: <https://jolt-lang.net/docs/differences.html>
- Jolt libraries: <https://jolt-lang.net/docs/libraries.html>
- Jolt REPL workflow: <https://jolt-lang.net/docs/repl-driven-development.html>
- Jolt testing: <https://jolt-lang.net/docs/testing.html>
- Babashka CLI: <https://github.com/babashka/cli>
- Glimmer TUI example:
  <https://github.com/jolt-lang/examples/tree/main/glimmer-tui-example>
- Glimmer TUI backend: <https://github.com/jolt-lang/glimmer-tui>
- Glimmer GTK backend: <https://github.com/jolt-lang/glimmer-gtk>
- Glimmer GTK app: <https://github.com/jolt-lang/examples/tree/main/glimmer-app>
- Glimmer article: <https://yogthos.net/posts/2026-08-29-glimmer-ui.html>
- Android findings: [`../../jolt/jolt-android/README.md`](../../jolt/jolt-android/README.md),
  its `docs/ARCHITECTURE.md`, `docs/GOTCHAS.md`, and `docs/DEVELOPMENT.md`
- modular dialect documentation example:
  `../../supplier-product-curator/spc-phel/AGENTS.md`, `src/README.md`, subtree
  READMEs, and `docs/DOCUMENTATION.md`
