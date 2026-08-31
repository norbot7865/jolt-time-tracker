# GTK frontend

GTK remains an optional host. The upstream reference is Glimmer GTK revision
`ce79d45698d36ccf496397bb85974e3cce6abfd8` (with Glimmer core
`5581c331c51aff989259b9e8e92ec920fe5e6741`). This Phase 0 environment has no
`DISPLAY` or `WAYLAND_DISPLAY`, so no window, screenshot, or live-reload claim
is made. GTK calls must stay on the appropriate UI thread; a future experiment
must use a documented Wayland compositor or controlled Xwayland fallback and
retain semantic assertions alongside safe synthetic screenshots.
