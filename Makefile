.DEFAULT_GOAL := help

.PHONY: help verify-bootstrap toolchain-required test repl build cli tui gtk android

help:
	@printf '%s\n' \
	  'Jolt Time Tracker (bootstrap)' \
	  '' \
	  '  make verify-bootstrap  validate repository structure and documentation' \
	  '  make test|repl|build|cli|tui|gtk|android  explain the current Phase 0 gate'

verify-bootstrap:
	@./scripts/verify-bootstrap

toolchain-required:
	@printf '%s\n' 'Jolt toolchain is not pinned or proven yet; complete Phase 0 toolchain bead first.' >&2
	@exit 2

test repl build cli tui gtk android: toolchain-required
