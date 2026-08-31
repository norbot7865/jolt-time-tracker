# Android host

## Phase 0 boundary evidence

The bounded Android runtime boundary is inherited from the independently pinned
Jolt Android PoC at revision `9f0e4c523f5bb04a5a98929e867809724ffe28c2`; the verifier currently
requires Jolt source revision `ae5c5a6d5be263a883e9b4b53f255b8c0b493d3e`.
Its reproducible verifier is:

```sh
cd ../jolt/jolt-android
JOLT_SOURCE=/path/to/jolt-at-ae5c5a6d nix develop -c ./scripts/verify
```

The PoC reports an `arm64-v8a` PIC shared library and debug APK, exported
bounded EDN calls, copied returned strings, single-owner runtime-thread
confinement, and ARM64 execution on an API-35 x86_64 emulator through
translation. It explicitly does **not** prove native ARM64 device execution.
Release artifacts omit the debug listener. This repository does not copy the
PoC's generated APK, libraries, screenshots, or caches; the product Android
host remains unimplemented until the portable wire contract bead is complete.

The evidence is therefore translated/emulator evidence, not native-device
evidence. The verifier's current Fedora run reproduced portable tests, ARM64
library ABI inspection, and debug APK assembly, then stopped because the API-35
x86_64 emulator did not boot within 180 seconds; repeated dispatch is therefore
not claimed for this run. Any future implementation must pin its Jolt/Chez/Android inputs again
and add a focused `jtt` verifier rather than treating this reference as proof
of product code.
