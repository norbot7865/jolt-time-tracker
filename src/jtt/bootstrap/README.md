# Composition roots

Hosts will use this area for independent composition roots. The initial
`smoke.clj` is a deliberately dependency-free Phase 0 proof that Jolt can load a
project namespace, run a deterministic `-main`, and compile it into a standalone
binary. It is not a product composition root. Future hosts must wire selected
adapters without making optional frontend or platform dependencies part of the
core closure.
