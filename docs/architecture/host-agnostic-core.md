# Host-agnostic core architecture

**Track:** `architecture/host-agnostic-core`  
**Current runtime target:** Minecraft 26.3 / NeoForge / Thaumcraft 4: Reforged 1.1.2

## Purpose and non-goals

The goal is to keep Forbidden Magic's content definitions and shared gameplay rules independent of a particular magic-host API. Host-specific registration, runtime behavior, entity semantics, item implementations, and lifecycle hooks belong behind explicit adapter contracts.

This work does **not** claim that the current JAR supports multiple magic hosts or multiple Minecraft versions. The production composition root still selects `Tc4HostAdapter`, and the current build still targets the 26.3 TC4 track. A second host needs its own compatible dependency/build track and a tested adapter. The existing Thaumaturge audit targets Minecraft 26.1.2 and explicitly records version and API blockers; it is not an implemented adapter.

## Layers

### 1. Shared model and catalogs — `core/`

The shared layer owns semantic values and definitions:

- `ContentId` identifies content without resolving it to a host registry object.
- `HostContent` describes host-owned item/tag references symbolically.
- Aspect, recipe, and research catalogs describe Forbidden Magic content as data.
- Shared wand behaviors use narrow access contracts rather than importing TC4 wand classes.

The architecture checker rejects Minecraft/NeoForge/host API imports from the pure model/catalog/API boundaries and rejects hard-coded host namespaces in core catalogs.

### 2. Host contract — `integration/api/`

`HostAdapter` is the registration/lifecycle contract. It exposes the selected host profile, aspect-tag registration, aspect/recipe/research registration, Crystalwell mode, diagnostics, and runtime bootstrap.

`HostProfile` declares feature support with explicit `NATIVE`, `ADAPTED`, `PARTIAL`, or `UNSUPPORTED` levels. A feature should be declared only when the adapter and tests support the claim.

`HostAdapterRegistry` is the capability lookup. `ForbiddenMagic` is the composition root: it installs the selected adapter before registering shared catalogs and content. Shared feature classes must not select or import a concrete adapter.

### 3. Runtime capability ports — `integration/runtime/`

Runtime contracts are separated by responsibility rather than combined in one service:

| Contract | Responsibility |
|---|---|
| `HostEntityAspectServices` | Typed entity-to-aspect catalog; declared by `ENTITY_ASPECTS` |
| `HostEntityClassificationServices` | Tainted-mob/boss/golem predicates and boss IDs; declared by `ENTITY_CLASSIFICATION` |
| `HostAspectServices` | Host-provided aspect display components |
| `HostMorphToolServices` | Morph-tool cycle sound and area mining |
| `HostBlockEntityServices` | Host-backed Wrath Cage block-entity factory |
| `HostWandServices` | Wand/focus queries, Vis storage, and accessory lookup |
| `HostTaintServices` | Taint materials, purification targets, taint damage, shovel behavior, and special mining drops |
| `HostScribingServices` | Research difficulty, primal aspect IDs, and player aspect-pool operations |
| `HostItemPropertyServices` | Host-specific `Item.Properties` customization; declared by `ITEM_PROPERTIES` |
| `HostItemFactoryServices` | Host-backed item implementations and remainder items; declared by `ITEM_FACTORIES` |
| `HostBootstrapServices` | Wand/trigger/optional-enchantment registration and creative-tab entries |

These ports may use Minecraft runtime types such as `ItemStack`, `LivingEntity`, `BlockState`, and `ServerPlayer`: this is a host-agnostic boundary within the current Minecraft/NeoForge target, not a cross-Minecraft-version abstraction. They must not expose TC4 or another magic-host's API types.

The old aggregate `HostRuntimeServices` and mixed `HostEntityServices` contracts were removed. Consumers request the narrow capability they need through `HostAdapterRegistry`.

### 4. Concrete host integration — `integration/tc4/`

TC4-specific registry values, entity classes, wand APIs, Essentia types, research/recipe APIs, Baubles, and host item subclasses live in this layer. `Tc4HostAdapter` implements the host contract and capability ports by delegating to focused TC4 adapters.

The one intended production composition-root reference to a concrete adapter is the selection of `Tc4HostAdapter.INSTANCE` in `ForbiddenMagic`. Other shared production classes should use the neutral contracts.

## Entity aspect catalog

The Mob Crystal/spawner lookup is now split:

- `FmSpawnerMobs` owns the common query surface and Wrath Cage imprint rules.
- `HostEntityAspectServices` supplies the `Map<ContentId, ContentId>` from entity IDs to aspect IDs.
- `HostEntityClassificationServices` supplies host-specific boss/taint/golem predicates and boss IDs.
- `Tc4EntityAdapter` owns the TC4 entity taxonomy and translates both entity and aspect identifiers to semantic `ContentId` values.

The silverfish configuration is passed to the host capability as an explicit option. The common mob catalog no longer hard-codes `thaumcraft:` entity or aspect identifiers.

## Validation

Every architectural slice should include all of the following:

1. Extend `tools/check_host_agnostic_api.py` to guard the new boundary. The checker scans all production Java files outside `integration/tc4`; the only allowed concrete-host selection is `Tc4HostAdapter` in `ForbiddenMagic`.
2. Add GameTest coverage for the adapter capability and the preserved gameplay semantics.
3. Run the pinned host preparation/build and the mod build.
4. Run GameTests in both `no_lust`/Luxuria configurations.
5. Keep artifact provenance and generated-resource checks green.

Static boundary checks catch unwanted dependencies; GameTests verify the current TC4 adapter. Neither is proof that a second host works. That claim requires an actual second adapter compiled and tested against its own compatible host version.

## Rules for future changes

- Keep semantic IDs and content definitions in `core/`; resolve them to host objects only in the concrete adapter.
- Do not add host API imports to shared gameplay, common facades, or capability contracts.
- Prefer a new narrow capability over adding unrelated methods to an existing contract.
- Add/adjust `HostProfile.Feature` declarations and tests whenever support changes.
- Keep Minecraft/NeoForge version tracks separate. Do not combine incompatible host versions in one source set or binary.
- Record partial or non-equivalent host behavior as `PARTIAL` and document the gap rather than silently approximating it.
