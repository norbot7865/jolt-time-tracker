# TUI frontend

Phase 0 pins Glimmer TUI revision
`9280e473bd58c7d442967b09342bf47a1c4a1cd0`. Its `buffer-screen` pipeline is
proven headlessly in `jtt.phase0.tui-test`, rendering a label and exposing
screen dimensions without loading ncurses. Interactive ncurses init/input/
resize/shutdown remains unproven until a pseudo-terminal harness is added;
blocking provider work must remain off the UI loop.
