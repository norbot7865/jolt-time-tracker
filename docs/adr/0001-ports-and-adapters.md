# ADR 0001: Ports and adapters

Status: accepted for Phase 1.

The provider, clock, configuration, and effect boundaries are data-oriented
ports. Concrete HTTP and persistence implementations stay in adapters; domain
namespaces do not load them. This keeps CLI, TUI, GTK, and Android composition
roots independent. The contracts are represented by `jtt.port.contracts` and
remain deliberately free of toolkit and JVM-only values.
