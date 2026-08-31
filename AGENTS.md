# Jolt Time Tracker agent instructions

Read [README.md](./README.md), [PLAN.md](./PLAN.md), and the applicable subtree
`README.md` before changing this repository. `PLAN.md` is a specification;
Beads in the parent `time-tracker-factory` repository is the work tracker.

## Required reading

Before Jolt work, read the current Jolt repository guidance at
[`../jolt/jolt/llms.txt`](../jolt/jolt/llms.txt) and user-facing index at
[`../jolt/jolt-lang.github.io/docs/llms.txt`](../jolt/jolt-lang.github.io/docs/llms.txt).
Read the relevant Jolt documentation, source, tests, known divergences, library
examples, and Android PoC material before making platform claims. Jolt is not a
JVM: do not assume Java interop, thread lifetime, filesystem, regex, string, or
time behavior without an experiment on the pinned runtime.

## Environment and safety

Inspect the host before assuming privileges, display services, KVM, native ARM,
or network access:

```sh
env | grep -E '^(HOSTNAME|JAI_|DISPLAY|WAYLAND_DISPLAY)=' || true
test -r /dev/kvm && ls -l /dev/kvm || true
```

The host is Wayland-first. Use a documented controlled fallback only as a test
harness and label it. Never commit generated outputs, caches, build products,
credentials, tokens, real provider payloads, or unsafe screenshots.

## Architecture and documentation

Dependencies point inward: domain code has no provider, HTTP, filesystem, UI,
or JNI imports; application code receives capabilities; adapters translate at
boundaries; frontends never import each other. Android calls cross a copied,
bounded EDN boundary on one runtime-owner thread.

Keep [src/README.md](./src/README.md) and affected parent/subtree READMEs current
when a responsibility or data flow changes. Record durable decisions in
`docs/adr/`, concrete architecture-affecting deltas in `docs/changes/`, and use
safe focused fenced `d2` diagrams where they clarify an observed boundary or
flow. `docs/ARCHITECTURE.md` contains only tested or experimentally observed
behavior; label proposed, observed, inferred, and blocked conclusions.

## Workflow

1. From the parent workspace, run `bd dolt pull` when configured, `bd prime`,
   `bd ready`, inspect a bead, and claim exactly one ready bead.
2. Start with the smallest explicit Jolt/nREPL experiment. Inspect the result,
   turn it into a focused test, then remove temporary probes.
3. Keep UI callbacks non-blocking. Visual work requires semantic assertions,
   safe screenshots, and vision-capable review; screenshots do not replace
   state assertions.
4. Run the affected fail-fast gates, inspect diagnostics and `git diff --check`,
   update narrow documentation and exact Beads evidence.
5. Make one focused atomic source commit per completed engineering bead, with
   its Beads ID in the message. Push source and Beads/Dolt state only after
   reviewing the correct repository and remote; do not commit unrelated parent
   work or alter the parent submodule pointer except in its designated bead.

Use Beads, not markdown task lists, for durable work state. Do not use
interactive `bd edit`. If a feasibility gate fails, preserve a reduced
reproduction and exact evidence; never silently substitute another runtime,
parser, UI toolkit, or platform.
