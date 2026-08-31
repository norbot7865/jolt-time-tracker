.DEFAULT_GOAL := help

JOLT ?= ./scripts/jolt
JOLT_ENV := JOLT_NO_USER_DEPS=1
SMOKE_BINARY := target/jtt-toolchain-smoke
JTT_BINARY := target/jtt
TUI_BINARY := target/jtt-tui
GTK_BINARY := target/jtt-gtk

.PHONY: help verify-bootstrap check-toolchain test repl nrepl-smoke build run-smoke clean \
	cli tui gtk android build-tui build-gtk

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
	  '  make build             build the standalone jtt CLI binary' \
	  '  make run-smoke         run the deterministic toolchain smoke binary' \
	  '  make cli               build and run the jtt CLI help command' \
	  '  make tui|gtk|android   fail until their dedicated host gates are complete'

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
	@$(JOLT_ENV) $(JOLT) build -m jtt.frontend.cli.core -o $(JTT_BINARY)

cli: build
	@./$(JTT_BINARY) help --json

build-tui:
	@mkdir -p target
	@$(JOLT_ENV) $(JOLT) build -m jtt.frontend.tui.core -o $(TUI_BINARY)

tui: build-tui
	@printf '%s\n' 'built target/jtt-tui; launch it from a usable terminal'

build-gtk:
	@mkdir -p target
	@$(JOLT_ENV) $(JOLT) build -m jtt.frontend.gtk.core -o $(GTK_BINARY)

gtk: build-gtk
	@printf '%s\n' 'built target/jtt-gtk; launch it on a GTK4 display server'

run-smoke: build
	@./$(SMOKE_BINARY)

clean:
	@rm -rf target .nrepl-port

android:
	@printf '%s gate is not proven yet; complete its dedicated Phase 0 bead first.\n' '$@' >&2
	@exit 2
