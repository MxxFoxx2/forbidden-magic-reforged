# Host-agnostic core architecture

**Track:** `architecture/host-agnostic-core`  
**Current runtime target:** Minecraft 26.3 / NeoForge / Thaumcraft 4: Reforged 1.1.2
**Status:** Implemented and CI-verified on this branch within the scope described below. This is not a claim of a second host adapter or cross-Minecraft-version compatibility.

## Purpose and non-goals

The goal is to keep Forbidden Magic's content definitions and shared gameplay rules independent of a particular magic-host API. Host-specific registration, runtime behavior, entity semantics, item implementations, and lifecycle hooks belong behind explicit adapter contracts.

This work does **not** claim that the current JAR supports multiple magic hosts or multiple Minecraft versions. The production composition root selects `Tc4HostAdapter`, and the current build targets the 26.3 TC4 track. Support for another magic host is outside the project's current scope.

## Layers

### 0. Shared gameplay and registries — `common/`

All shared mod classes (blocks, items, `Fm*` registries, config, event handlers) live in
`dev.visfox.forbiddenreforged.common`. The root package holds only the composition root
`ForbiddenMagic`; `common/FmIds` owns `MODID`/`LOGGER`/`id()` so no shared class imports the
composition root. `tools/check_host_agnostic_api.py` rejects any import of the root package
outside `ForbiddenMagic.java` and any `integration/tc4` dependency on the root package, which
keeps the package graph acyclic: `integration/tc4 → common → integration/api → core`.

### 0b. GameTests — `src/gameTest`

GameTest classes live in a dedicated `gameTest` source set (`src/gameTest/java`), wired only
into the `gameTestServer` run and the mod's test namespace. They are not compiled into the
production JAR, and `ForbiddenMagic` no longer references them.

### 0c. Content-gating config sync — `common/FmSync`

`FmConfig` stays `STARTUP` (read before registration), but the flags that change registered
content (`no_lust`, `emerald_transmutation`, `enchanting`, `greedy_ench`, `wrath_cage`) are
sent to clients in a play-to-client payload on login; a mismatch disconnects the client with
an explanatory message because registries cannot be reconciled after the fact. Display-only
flags (`tag_research`, `spork`) remain local. `tools/check_host_pin.py` (scheduled CI) verifies
the pinned host sha and patch applicability without ever updating `host.sha`.

### 1. Shared model and catalogs — `core/`

The shared layer owns semantic values and definitions:

- `ContentId` identifies content without resolving it to a host registry object.
- `HostContent` describes selected-host item/tag references symbolically using the internal `host:item/...` and `host:tag/...` keys; no concrete host namespace is embedded in the model.
- Aspect, recipe, and research catalogs describe Forbidden Magic content as data.
- Shared wand behaviors use narrow access contracts rather than importing TC4 wand classes.

The architecture checker rejects Minecraft/NeoForge/host API imports from the pure model/catalog/API boundaries and rejects hard-coded host namespaces in core catalogs.

### 2. Host contract — `integration/api/`

`HostAdapter` is the registration/lifecycle contract. It exposes the selected host profile, aspect-tag registration, aspect/recipe/research registration, Crystalwell mode, diagnostics, and runtime bootstrap.

`HostProfile` advertises feature support with `NATIVE`, `ADAPTED`, `PARTIAL`, or `UNSUPPORTED` levels. It is descriptive compatibility metadata, not a runtime feature switch. An omitted entry is treated as `UNSUPPORTED` by `supportOf()`. Keep the declarations accurate; test material `PARTIAL`/`UNSUPPORTED` limitations and actual behavior rather than copying the entire metadata table into GameTests.

`HostAdapterRegistry` is the capability lookup. `ForbiddenMagic` is the composition root: it installs the selected adapter before registering shared catalogs and content. Shared feature classes must not select or import a concrete adapter.

### 3. Runtime capability ports — `integration/runtime/`

Runtime contracts are separated by responsibility rather than combined in one service:

| Contract | Responsibility |
|---|---|
| `HostEntityAspectServices` | Typed entity-to-aspect catalog; declared by `ENTITY_ASPECTS` |
| `HostEntityClassificationServices` | Tainted-mob/boss/golem predicates and boss IDs; declared by `ENTITY_CLASSIFICATION` |
| `HostAspectServices` | Host-provided aspect display components |
| TC4 aspect registration | Declares `ASPECT_RECIPE_DERIVATION` because semantic component lists are passed into the host AddonAspects registrar |
| `Tc4WarpAdapter` | Maps `PERMANENT`, `STICKY`, and `TEMPORARY` to distinct TC4 pools; declared by `WARP_POOLS` and `STICKY_WARP` |
| `HostMorphToolServices` | Morph-tool cycle sound and area mining |
| `HostBlockEntityServices` | Host-backed Wrath Cage block-entity factory |
| TC4 Wrath Cage bridge | Declares `ESSENTIA_STORAGE` and `ESSENTIA_TRANSPORT` because its host subclass implements `AspectSource` and `EssentiaTransport` |
| `HostWandInspectionServices` | Read-only wand and focus queries; declared by `WAND_INSPECTION` |
| `HostWandVisServices` | Writes semantic aspects to wand and real Vis stores; declared by `WAND_VIS_STORAGE` |
| `Tc4WandPartsAdapter` | Implements rod replacement through the TC4 `WandParts` component; declared by `WAND_PARTS` |
| `HostAccessoryServices` | Host accessory slot lookup; declared by `ACCESSORY_INTEGRATION` |
| `HostTaintBlockServices` | Taint block classification and taint-shovel behavior; declared by `TAINT_BLOCK_SERVICES` |
| `HostEnchantmentEffectServices` | Host-specific special-mining drops and taint damage classification; declared by `ENCHANTMENT_EFFECTS` |
| `HostScribingServices` | Research difficulty, primal aspect IDs, and player aspect-pool operations |
| `HostItemPropertyServices` | Host-specific `Item.Properties` customization; declared by `ITEM_PROPERTIES` |
| `HostItemFactoryServices` | Host-backed item implementations and remainder items; declared by `ITEM_FACTORIES` |
| `HostBootstrapServices` | Wand/trigger/optional-enchantment registration and creative-tab entries |
| `HostWarpServices` | Applies semantic permanent/sticky/temporary warp changes through `WarpAccess`; host mapping remains in `Tc4WarpAdapter` |

These ports may use Minecraft runtime types such as `ItemStack`, `LivingEntity`, `BlockState`, and `ServerPlayer`: this is a host-agnostic boundary within the current Minecraft/NeoForge target, not a cross-Minecraft-version abstraction. They must not expose TC4 or another magic-host's API types.

The old aggregate contracts were removed. Consumers request the narrow capability they need
through the typed accessors of `HostAdapterRegistry` (`warp()`, `wandVis()`, `itemFactories()`, ...),
which read the adapter's immutable `HostServices` composition.

### 4. Concrete host integration — `integration/tc4/`

TC4-specific registry values, entity classes, wand APIs, Essentia types, research/recipe APIs, Baubles, and host item subclasses live in this layer. `Tc4HostAdapter` implements only the registration/lifecycle `HostAdapter` contract and exposes `services()`; `Tc4Services` composes every runtime port from the focused TC4 adapters in one place.

The one intended production composition-root reference to a concrete adapter is the selection of `Tc4HostAdapter.INSTANCE` in `ForbiddenMagic`. Other shared production classes should use the neutral contracts.

## Entity aspect catalog

The Mob Crystal/spawner lookup is now split:

- `FmSpawnerMobs` owns the common query surface and Wrath Cage imprint rules.
- `HostEntityAspectServices` supplies the `Map<ContentId, ContentId>` from entity IDs to aspect IDs.
- `HostEntityClassificationServices` supplies host-specific boss/taint/golem predicates and boss IDs.
- `Tc4EntityAdapter` owns the TC4 entity taxonomy and translates both entity and aspect identifiers to semantic `ContentId` values.
- `Tc4WarpAdapter` maps all three semantic warp kinds directly to TC4 `WarpType` values, including distinct `STICKY`; the profile declares `WARP_POOLS` and `STICKY_WARP`. GameTests exercise all three mappings and a sticky-warp change through `HostWarpServices`.
- Core wand progression uses `WandContractAccess` for the Profane Contract component and `WarpAccess` for sticky-warp mutations. The concrete data-component adapter lives outside `core`; the host warp operation is exposed through `HostWarpServices`. No core warp class extends NeoForge `Event` or posts to the NeoForge event bus.
- Host taint-origin registration is not implemented; `TAINT_ORIGINS` is explicitly declared `UNSUPPORTED` by the TC4 adapter. Every `HostProfile.Feature` has one explicit support declaration.

The silverfish configuration is passed to the host capability as an explicit option. The common mob catalog no longer hard-codes `thaumcraft:` entity or aspect identifiers.

## Validation

Use checks that match the change:

1. Use `tools/check_host_agnostic_api.py` to guard package and import boundaries; extend it only when a real boundary changes.
2. Add GameTests for changed runtime behavior, compatibility-sensitive paths, and meaningful host limitations. Do not mirror the full `HostProfile` metadata table in tests.
3. For integration changes, run the pinned host preparation/build and the relevant GameTests. The full release CI gate remains responsible for provenance, generated-resource freshness, and both `no_lust` configurations.

Static boundary checks catch unwanted dependencies; GameTests verify the current TC4 adapter. They do not imply compatibility with other magic hosts or Minecraft versions.

## Rules for future changes

- Keep semantic IDs and content definitions in `core/`; resolve them to host objects only in the concrete adapter.
- Do not add host API imports to shared gameplay, common facades, or capability contracts.
- Prefer a new narrow capability over adding unrelated methods to an existing contract.
- Update `HostProfile` when host support materially changes. Test the important `PARTIAL`/`UNSUPPORTED` limitations and affected behavior instead of adding a test for every metadata entry.
- Keep Minecraft/NeoForge version tracks separate. Do not combine incompatible host versions in one source set or binary.
- Record partial or non-equivalent host behavior as `PARTIAL` and document the gap rather than silently approximating it.
