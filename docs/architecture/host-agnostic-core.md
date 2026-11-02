# Host-agnostic core architecture — Minecraft 1.21.1

**Track:** `architecture/1.21.1-host-agnostic`  
**Runtime target:** Minecraft 1.21.1 / NeoForge 21.1.256 / Thaumcraft 4: Reforged 1.1.2 / Java 21  
**Status:** Architecture port implemented in this draft branch and verified by CI baseline [#118](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/41377991137). This does not claim that every legacy TC4 integration has already been extracted.

## Purpose and non-goals

The portability boundary is the **magic host**, not Minecraft. Shared definitions and behavior contracts should not depend on Thaumcraft's registry, wand, aspect, research, recipe, Baubles, or Essentia classes. Minecraft and NeoForge remain first-class runtime dependencies.

The 1.21.1 runtime is a separate track. Its adapter uses the 1.21.1 `ResourceLocation`, damage, particle, item-property, block-entity, and Thaumcraft APIs. It is not source- or binary-compatible with the newer 26.x tracks merely by changing version constants.

This branch does not produce a universal JAR across Minecraft versions, does not alter `main` or `mc/26.1.2`, and does not claim a second production magic-host adapter.

## Layers

### Shared semantic model — `core/`

The shared core owns:

- `ContentId` and symbolic host content references.
- Aspect, recipe, and research definitions/catalogs.
- Shared wand behavior rules and narrow access contracts, including Blink Focus targeting, teleport effects, upgrade behavior, and Witchwood mana conversion rates.
- Shared Wrath Cage spawn eligibility, delay calculation, bounded Essentia insertion, extraction eligibility, and fuel-channel priority rules.
- Semantic warp kinds and access contracts.

The core may use Minecraft runtime types where behavior necessarily operates on Minecraft objects, such as `ItemStack`, entities, and levels. It must not import Thaumcraft or NeoForge implementation APIs, or refer to the concrete TC4 adapter.

### Adapter contract — `integration/api/`

- `HostAdapter` describes the selected host's catalog registration, diagnostics, bootstrap, and profile.
- `HostProfile` declares every capability as `NATIVE`, `ADAPTED`, `PARTIAL`, or `UNSUPPORTED`.
- `HostAdapterRegistry` installs the selected adapter once and exposes typed capability lookups.

`ForbiddenMagic` is the composition root. It installs `Tc4HostAdapter.INSTANCE` before shared aspect catalog registration and before item registration causes host item properties/factories to be resolved.

### Runtime capabilities — `integration/runtime/`

The narrow interfaces currently cover aspect services, item properties and factories, entity aspects and classification, morph tools, block-entity construction, wand inspection and Vis storage, accessory slots, taint block services, enchantment effects, scribing operations, warp, and bootstrap.

These contracts may expose Minecraft/NeoForge runtime types required by the 1.21.1 implementation. They must not expose Thaumcraft types or concrete adapter classes.

### TC4 implementation — `integration/tc4/`

The concrete layer owns conversions between semantic identifiers and TC4 values, TC4 aspect/recipe/research registration, wand and Vis calls, Baubles, taint damage, special mining, Thaumcraft entity classification, TC4 item properties/factories, and warp-pool mapping.

The original 1.21.1 gameplay implementation is retained for legacy features that are not fully migrated yet. `BlinkFocusItem` remains the TC4 `FocusItem` integration wrapper, while targeting, destination calculation, teleport effects, and upgrade-specific combat behavior are delegated to the shared `core/wand/BlinkFocusBehavior`. `WrathCageBlockEntity` remains the TC4 block-entity/Essentia transport implementation, but spawn eligibility, delay calculation, per-channel insertion limits, extraction eligibility, and fuel-channel priority are delegated to `core/block/WrathCageBehavior`. The transport calls that physically extract Essentia, persistence, and entity spawning remain host/runtime work. Existing compatibility facades such as `FmAspects` and `FmWands` remain transitional; `FmWands` now delegates Witchwood mana rules to the shared core rather than duplicating the formulas.

## Current capability status

The 1.21.1 `Tc4HostAdapter` declares every `HostProfile.Feature`. The currently adapted capabilities include:

- Aspect registration, diagnostics, presentation, and scan targets.
- Semantic recipe and research catalog registration for the supported recipe/page forms.
- Warp pools and sticky warp mapping.
- Morph-tool area mining and cycle sound.
- Wand inspection and wand/real Vis writes.
- Baubles accessory lookup.
- Taint material classification, taint-shovel behavior, and special-mining/taint-damage hooks.
- Scribing research difficulty, primal-aspect selection, and aspect-pool updates.
- Wrath Cage block-entity construction.
- Entity-to-aspect catalog and host entity classification.
- Host-specific item properties and factories.

Known limits remain explicit:

| Capability | Current declaration | Limitation |
|---|---|---|
| Research stages | `PARTIAL` | Adapter accepts one page group; multi-stage definitions are rejected |
| Wand rods, caps, triggers, and parts | `PARTIAL` | Existing 1.21.1 registration remains in the legacy gameplay path |
| Focus/spell runtime | `PARTIAL` | Not fully extracted into a host capability |
| Essentia storage/transport | `PARTIAL` | Shared spawn, storage, extraction-eligibility, and fuel-selection policies are extracted; Wrath Cage still extends TC4 block-entity and transport types directly, and physical transport/persistence remain host-bound |
| Optional enchantment integration | `PARTIAL` | Legacy optional-mod path remains |
| Taint origins | `UNSUPPORTED` | No taint-origin registration or propagation adapter is implemented |

All unlisted capabilities remain `PARTIAL` until their consumers and tests are moved behind the corresponding interface. Do not upgrade a status just because an interface exists.

## Validation

The 1.21.1 workflow builds the pinned Thaumcraft host and addon, verifies artifact provenance, runs GameTests, and uploads the resulting JAR. The architecture boundary checker scans `core/`, `integration/api/`, and `integration/runtime/` and rejects direct Thaumcraft, NeoForge implementation, or concrete TC4-adapter dependencies in those scopes.

The GameTest `hostAdapterDeclaresThe1_21_1CapabilityProfile` asserts the selected host/Minecraft versions, full feature coverage, and representative `ADAPTED`, `PARTIAL`, and `UNSUPPORTED` declarations.

A green headless CI run verifies compilation and GameTest behavior; it does not replace manual client checks for rendering, tooltips, wand/focus interactions, and in-world visuals before release.

## Rules for future changes

1. Put shared definitions and semantics in `core/`; resolve them to host objects only inside `integration/tc4/`.
2. Prefer a narrow runtime capability over a growing aggregate host interface.
3. Move common consumers to capability contracts before marking a feature `ADAPTED`.
4. Update the profile and its GameTest whenever support changes.
5. Keep MC/NeoForge version tracks and binaries separate.
6. Preserve legacy gameplay behavior while moving implementation behind the adapter; do not silently replace mechanics with approximations.
