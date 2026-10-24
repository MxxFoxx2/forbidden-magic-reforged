# JEI dev runtime and aspect diagnostics implementation plan

**Goal:** Load JEI in the development client and expose Forbidden Magic aspect registration, ordinals, and components through a startup summary and `/forbiddenmagic aspects`.

**Architecture:** JEI is a full runtime dependency only of a separate `client` source set used by `runClient`. The common `main` source set and `gameTestServer` remain JEI-free. A JEI-independent formatter is the single source of truth for the startup log and command output.

**Tech Stack:** Java 25, Minecraft 26.3, NeoForge 26.3.0.57-beta, ModDevGradle 2.0.148, JEI 31.16.0.90, NeoForge GameTests.

**Spec:** `docs/superpowers/specs/2026-10-24-jei-aspect-diagnostics-design.md`

## Global Constraints

- The published Forbidden Magic jar must contain no JEI classes.
- JEI must not load in `gameTestServer`; it is a development-client runtime only.
- JEI 31.16.0.90 is the only JEI version used by this slice.
- Built-in aspects retain ordinals `0-47`; Forbidden Magic aspects begin at ordinal `48` in deterministic registration order.
- `no_lust` removes only `luxuria`; the remaining six add-on aspects still register at consecutive ordinals.
- The host is pinned: do not edit `../thaumcraft-4-reforged` or `host/` directly.
- Tests are GameTests only; Java uses four-space indentation and no comments.
- Commits use English messages and explicit paths only; do not use `git add -A` or `git commit -a`.

## Task 1: Add JEI to the development client only

**Files:**
- Modify: `gradle.properties`
- Modify: `build.gradle`

- [x] Pin `jei_version=31.16.0.90` and add the BlameJared Maven repository with content filtering for `mezz.jei` and `net.mezzdev.config`.
- [x] Create a `client` source set whose runtime classpath includes the main output and extends the main runtime classpath.
- [x] Declare the full JEI NeoForge artifact as `clientRuntimeOnly` and point the `client` run at `sourceSets.client`.
- [x] Keep `gameTestServer` on `sourceSets.main`.
- [x] Verify `clientRuntimeClasspath` contains JEI while `runtimeClasspath` does not.

## Task 2: Add authoritative aspect diagnostics and the in-game command

**Files:**
- Create: `src/main/java/dev/visfox/forbiddenreforged/FmAspectDiagnostics.java`
- Create: `src/main/java/dev/visfox/forbiddenreforged/FmCommands.java`
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmSetup.java`
- Modify: `tests/java/dev/visfox/forbiddenreforged/test/AspectGameTests.java`

- [x] Add a failing GameTest for the stable registry table.
- [x] Implement `FmAspectDiagnostics.registryLines()` and `addonLines()` using `Aspect.values()`, `Aspect.builtinCount()`, `Aspect.ordinal()`, and `Aspect.getComponents()`.
- [x] Add a failing GameTest for `/forbiddenmagic aspects` command registration.
- [x] Implement the game-master command with literal stable output.
- [x] Log the total, built-in, add-on, expected add-on count, `luxuria` status, and each add-on row during common setup.
- [x] Run the GameTest gate and confirm all required tests pass.

## Task 3: Record the diagnostic addition and run the release gate

**Files:**
- Modify: `docs/fidelity.md`

- [x] Record the diagnostic command and startup summary in `docs/fidelity.md` as an inspection aid that does not change gameplay.
- [x] Run `python tools/prepare_host.py --verify`.
- [x] Run `python tools/check_provenance.py`.
- [x] Run `cmd //c gradlew.bat build runGameTestServer`.
- [x] Inspect the published jar and confirm it contains no JEI classes.
- [x] Confirm the GameTest server log does not load JEI.
- [x] Commit the implementation and documentation with explicit paths.
- [ ] User performs the visual `runClient` check for JEI opening and the in-game command table.
