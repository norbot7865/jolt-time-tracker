# Provider contracts

## Status

Provider behavior is **unimplemented**. The Go implementation in
[`../../gtt/`](../../gtt/) is the initial behavior oracle; external API claims
must be verified with synthetic local contract servers before live credentials
are ever used.

## Intended boundary

The application sees canonical provider-neutral maps and focused capabilities.
Adapters alone own authentication headers, endpoint paths, DTOs, query/body
shapes, wire timestamps, tag representation, and provider-specific IDs.

- Clockify uses its API key header and opaque identifiers at the adapter edge.
- Kimai 2.x uses bearer authentication, numeric identifiers, and its own
  project/activity representations at the adapter edge.
- Provider DTOs must never become domain values or UI state.

Each adapter bead must document the pinned API evidence, local-server requests,
response normalization, limits, malformed/non-2xx handling, and redaction.
