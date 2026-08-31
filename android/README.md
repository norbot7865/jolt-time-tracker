# Android host

Reserved for the deliberately minimal Kotlin/Gradle/JNI/Compose host. It will
load the pinned ARM64 Jolt library, confine calls to one owner thread, and execute
bounded effects returned by portable contracts. Android status is unproven until
its dedicated Phase 0 experiment records reproducible native or clearly labeled
translated evidence.
