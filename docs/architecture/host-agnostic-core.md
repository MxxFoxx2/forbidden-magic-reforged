# Host-agnostic Forbidden Magic architecture

## Goal

Keep Forbidden Magic's game rules and content definitions independent from the lifecycle and
Java types of any one thaumaturgy host. A host adapter translates those definitions into the
host's public API. Losing or changing one host must not require rewriting the common gameplay
rules.

This does **not** make one binary compatible with multiple Minecraft versions. Minecraft version
targets remain separate build/release tracks.

## Target boundaries

- **Core model** — stable identifiers and immutable descriptions of aspects, recipes, research,
  item ingredients, outputs and other Forbidden Magic concepts. No Minecraft, NeoForge, TC4 or
  Thaumaturge imports.
- **Core gameplay** — mechanics whose behavior belongs to Forbidden Magic: drops, tool behavior,
  enchantment effects, taint blocks, Mob Crystal logic and similar features. It may use Minecraft
  APIs, but not host-specific Java classes.
- **Integration API** — narrow contracts between common code and adapters. It describes host
  capabilities and registration/runtime operations without exposing host types.
- **Host adapters** — separate implementations for Thaumcraft 4: Reforged, Thaumaturge or a
  future host. Only adapters may import the corresponding host API.
- **Minecraft target glue** — version-specific registration, rendering and platform code. It is
  not forced into a cross-version abstraction.

## Repository package direction

The first implementation slice introduces:

- `core/model`: host-neutral namespaced content identifiers;
- `core/aspect`: aspect definitions, semantic blend modes, costs and the current Forbidden Magic
  aspect catalog;
- `core/recipe`: host-neutral recipe descriptions for infusion, crucible and arcane crafting;
- `core/research`: research entries, stages, pages and prerequisites;
- `integration/api`: host capability profiles and the registration SPI;
- `integration/tc4`: the first host-specific translation, currently for aspect registration.

These classes are deliberately pure Java outside the adapter package. They are currently compiled
as part of the existing mod source set; extracting a separately published Gradle module is deferred
until there is a real consumer and the artifact/dependency packaging strategy is settled.

## Rules

1. Common model and gameplay packages must not import `thaumcraft.*`,
   `com.leclowndu93150.thaumaturge.*` or host implementation packages.
2. Common model packages must not import `net.minecraft.*` or `net.neoforged.*`.
3. Adapters translate semantic `ContentId` values into host-native identifiers/holders and
   record non-equivalent mappings as PARTIAL or ADAPTED in `HostProfile`.
4. Missing host functionality is explicit. Do not silently substitute a similar aspect,
   enchantment, warp pool or recipe mechanic and claim exact parity.
5. Do not introduce a universal service locator, a single god-interface for all host behavior,
   or plugin discovery machinery without a concrete requirement.
6. Keep host-specific recipe serializers, registries, capabilities and lifecycle scheduling inside
   the corresponding adapter.
7. Keep Minecraft-version-specific code on its existing version track. A common domain model
   does not imply binary or source compatibility across 1.21.1, 26.1.2 and 26.3.
8. Preserve the current TC4 behavior while the new contracts are introduced. Migrate one vertical
   slice at a time and keep the existing fidelity ledger and GameTests as regression gates.

## Registration lifecycle

The initial `HostAdapter` SPI separates aspect registration from recipe and research
registration because the current TC4 integration initializes aspects before item classes capture
them, then registers recipes and research during common setup. Adapters remain responsible for
scheduling each phase at the appropriate point in their host's lifecycle.

The first SPI intentionally covers declarative content registration only. Runtime ports for
warp, wand vis, essentia transport, focus/spell operations and taint are introduced separately
when their first common gameplay consumers are migrated.

## First migrated slice: aspects

The common `ForbiddenMagicAspectCatalog` now owns the seven aspect definitions, their colors,
semantic component references, texture identifiers and blend modes. The `concept/*` component
IDs are Forbidden Magic concepts, not TC4 or Thaumaturge registry IDs.

`Tc4AspectAdapter` translates those concepts to TC4's `Aspect` constants and translates the
semantic blend modes to the integer values expected by `AddonAspects`. `FmAspects` remains a
small compatibility facade because existing recipes, focus upgrades and mechanics refer to its
static TC4 `Aspect` fields. Its initialization order has not been changed: it still runs before
items and focus upgrades are initialized.

This is a source migration, not yet a proven runtime migration. Existing aspect GameTests and
the full host build must pass before the slice is considered complete. The Thaumaturge mapping is
not implemented here; its missing base aspects and alternative compositions must be reported as
adaptations rather than hidden in the common catalog.

## Migration sequence

1. **Foundation (implemented):** pure-Java domain models, adapter profile and registration SPI.
2. **Aspect source migration (implemented, verification pending):** move Forbidden Magic aspect
   definitions out of `FmAspects` and translate them through the TC4 adapter. Verify IDs,
   composition, colors, blend modes, registration order and aspect scans.
3. **Recipe vertical slice:** move recipe definitions into the common model; translate to TC4
   recipe serializers and retain recipe/research links.
4. **Research vertical slice:** move categories, entries, stages and page definitions; preserve
   progression, flags, warp and translations.
5. **Runtime mechanics:** add narrow ports for essentia, warp, wand vis/parts and spells only when
   common gameplay code needs them.
6. **Second host:** implement Thaumaturge against its public API and record genuine semantic gaps
   in its profile. Do not use the previous `port/thaumaturge` branch as the architecture base.
7. **Extraction:** consider separate Gradle modules only after source boundaries and packaging have
   been proven in a working adapter.

## Current status and non-claims

The pure-Java model and TC4 aspect translation are committed on this branch, but no runtime or
GameTest result has yet been confirmed for these changes. The existing `FmRecipes`, `FmResearch`,
`FmWands` and most gameplay classes still use TC4 types directly. The general `HostAdapter` SPI
is not yet wired into mod startup; `Tc4AspectAdapter` is the first focused adapter slice. No
Thaumaturge adapter is implemented.

The current `main` branch is unchanged. Merge only after the branch build, both existing
GameTest configurations, the architecture boundary check and relevant in-game aspect checks pass.
