# JEI dev runtime and aspect diagnostics

## Intent

Add JEI to the development client without making it a runtime dependency of the published Forbidden Magic add-on, and provide visible diagnostics that prove the patched add-on aspect API registered every Forbidden Magic aspect without changing the built-in aspect indexes.

The approved scope is: JEI in the development client, `/forbiddenmagic aspects`, and an automatic startup summary. The host already owns Thaumcraft's JEI categories and recipe strips, so this slice does not add a separate JEI plugin or ordinary item tooltip integration.

## Goals

- Load JEI in `runClient` for Minecraft 26.3 / NeoForge 26.3.
- Keep JEI absent from the published add-on jar, the common runtime classpath, and `gameTestServer`.
- Show the complete aspect registry, including built-in indexes `0-47` and Forbidden Magic's add-on indexes.
- Show the same stable table in-game and in the startup log.
- Record the diagnostic addition in `docs/fidelity.md` because Forbidden Magic 0.574 did not provide it.

## Non-goals

- Do not add a separate JEI aspect category or aspect ingredients.
- Do not add a JEI API plugin or JEI API dependencies to the add-on.
- Do not add object-aspect lines to ordinary item tooltips.
- Do not change aspect registration order, names, colors, components, textures, or gameplay behavior.
- Do not modify the pinned host repository or host patches.

## Selected approach

### Development runtime

Pin JEI `31.16.0.90` in `gradle.properties` and use the official BlameJared Maven repository. Minecraft 26.3 no longer supports ModDevGradle's legacy `clientAdditionalRuntimeClasspath`, so the client run uses a separate `client` source set. Its runtime classpath extends the main runtime classpath, and the full JEI NeoForge artifact is declared only as `clientRuntimeOnly`. The `gameTestServer` run continues to use `sourceSets.main` and therefore does not resolve JEI.

### Aspect registry diagnostics

Add a small diagnostic formatter based on the public `Aspect.values()` registry. It reports each aspect's tag, display name, ordinal, built-in/add-on classification, and components. The built-in range is derived from `Aspect.builtinCount()`, while the Forbidden Magic identities come from the add-on's `FmAspects` fields.

`/forbiddenmagic aspects` prints this table in-game for operator-level inspection. The same formatter is used for an automatic INFO summary after common setup, making registration failures and ordinal shifts visible in `latest.log` without requiring a command.

## Failure behavior

- A missing host add-on aspect API remains a fatal startup error with the existing actionable message.
- Missing JEI does not prevent the add-on from loading in a normal deployment because JEI is not a published dependency.
- The startup summary identifies the expected add-on count and built-in ordinal boundary.
- Under `no_lust=true`, `luxuria` is reported as `disabled-by-config`; an unexpected missing `luxuria` is reported as `missing-registration-error`.

## Verification

- Run the complete build and GameTest server gate and record the required-test count.
- Verify the pinned host with `python tools/prepare_host.py --verify`.
- Run the provenance check.
- Confirm the client runtime dependency report contains JEI and the common runtime report does not.
- Confirm the GameTest server log does not load JEI.
- Confirm the published jar contains no JEI classes.
- A user-run `runClient` check confirms JEI opens and `/forbiddenmagic aspects` is visible in-game.
