.DEFAULT_GOAL := help

JOLT ?= ./scripts/jolt
JOLT_ENV := JOLT_NO_USER_DEPS=1
SMOKE_BINARY := target/jtt-toolchain-smoke

.PHONY: help verify-bootstrap check-toolchain test repl nrepl-smoke build run-smoke clean \
	cli tui gtk android

help:
	@printf '%s\n' \
	  'Jolt Time Tracker' \
	  '' \
	  'Set JOLT_BIN=/absolute/path/to/pinned/jolt when jolt is not on PATH.' \
	  '' \
	  '  make verify-bootstrap  validate repository structure and documentation' \
	  '  make check-toolchain  print the selected Jolt version' \
	  '  make test              run portable Jolt tests with user deps disabled' \
	  '  make repl              start a clean line REPL' \
	  '  make nrepl-smoke       prove a live nREPL eval request' \
	  '  make build             build the deterministic standalone smoke binary' \
	  '  make run-smoke         run the built smoke binary' \
	  '  make cli|tui|gtk|android  fail until their Phase 0 gates are complete'

verify-bootstrap:
	@./scripts/verify-bootstrap

check-toolchain:
	@$(JOLT_ENV) $(JOLT) --version

test:
	@set -e; \
	  python3 scripts/http-fixture.py & fixture=$$!; \
	  trap 'kill $$fixture 2>/dev/null || true' EXIT INT TERM; \
	  $(JOLT_ENV) $(JOLT) -M:test

repl:
	@$(JOLT_ENV) $(JOLT) repl

nrepl-smoke:
	@$(JOLT_ENV) ./scripts/nrepl-smoke

build:
	@mkdir -p target
	@$(JOLT_ENV) $(JOLT) build -m jtt.bootstrap.smoke -o $(SMOKE_BINARY)

run-smoke: build
	@./$(SMOKE_BINARY)

clean:
	@rm -rf target .nrepl-port

cli tui gtk android:
	@printf '%s gate is not proven yet; complete its dedicated Phase 0 bead first.\n' '$@' >&2
	@exit 2
