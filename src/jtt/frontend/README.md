# Frontends

Host-specific interaction layers consume shared application events and view
models. CLI, TUI, and GTK are optional and must never import one another. Widget
state, focus, dialogs, and toolkit scheduling remain host-owned.
