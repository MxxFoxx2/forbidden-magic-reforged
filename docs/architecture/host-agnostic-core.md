# Host-agnostic core on Minecraft 26.1.2

**Target:** Minecraft 26.1.2 / NeoForge 26.1.2.122 / Thaumcraft 4: Reforged 1.1.2  
**Implementation:** `architecture/26.1.2-host-agnostic`  
**Status:** Implemented and CI-verified for this release track. Client-side visual and manual gameplay checks remain separate.

## Architectural boundary

Forbidden Magic remains a Minecraft mod. Minecraft and NeoForge types are valid in shared runtime code wherever game behavior needs them.

The portability boundary is the **magic host** — the magic system that provides aspects, research, recipes, wands, Vis, warp, taint behavior, Essentia, and related services. Those host-specific APIs and implementations belong behind adapter contracts. This is not a Minecraft-independent engine and does not attempt to hide Minecraft or NeoForge behind another abstraction.

Minecraft/NeoForge version differences remain isolated in separate release tracks. The 26.1.2, 26.3, and 1.21.1 builds are not one source set or one interchangeable JAR.

## Layers

### 1. Shared core — `core/`

The shared layer describes Forbidden Magic content and reusable rules without resolving them into Thaumcraft registry objects:

- `core/model`: semantic `ContentId` values and symbolic host-owned item/tag references through `HostContent`.
- `core/aspect`, `core/recipe`, and `core/research`: data catalogs and definitions for aspects, recipes, research categories, and research entries.
- `core/wand` and `core/warp`: reusable wand/warp behavior expressed through focused access ports.

The core can use Minecraft types such as `ItemStack`, `Player`, and `ServerPlayer` where that is intrinsic to the gameplay interaction. It must not import Thaumcraft types or hard-code Thaumcraft registry namespaces.

### 2. Host contract — `integration/api/`

- `HostAdapter` defines host operations for aspect, recipe, and research registration, aspect scan targets, runtime bootstrap, and the host profile.
- `HostProfile` records the host and Minecraft versions and the support level of each declared feature.
- `HostAdapterRegistry` installs one selected adapter and exposes it, or its focused capabilities, to consumers.

### 3. Runtime capability ports — `integration/runtime/`

Host-provided runtime behavior is split into narrow contracts rather than a single general-purpose service. The current ports cover entity aspect catalogs and classification, aspect presentation, Morph Tool behavior, block entities, wand inspection and Vis, accessory lookup, taint blocks, enchantment effects, scribing, warp, item properties/factories, and startup/creative-tab bootstrap.

Shared consumers request only the capability they need through `HostAdapterRegistry`. These interfaces may use Minecraft runtime types, but must not expose concrete Thaumcraft API types.

### 4. Thaumcraft implementation — `integration/tc4/`

The `Tc4HostAdapter` and its focused components translate the shared contracts into Thaumcraft 4: Reforged 1.1.2 behavior: native aspect/recipe/research registries, wand APIs, Vis storage, warp pools, entity taxonomy, taint classification, accessory slots, item implementations, and the Wrath Cage Essentia bridge.

## Adapter lifecycle

1. `ForbiddenMagic` installs `Tc4HostAdapter.INSTANCE` as the selected host adapter.
2. The mod registers the common aspect definitions, scan targets, and Minecraft-owned content.
3. The common setup event calls `HostAdapterRegistry.require().initializeRuntime()`; the TC4 bootstrap then registers host-dependent wands, triggers, recipes, research, warp item values, and optional integrations.
4. Common gameplay code uses the host contract/capability ports rather than selecting or importing `Tc4HostAdapter` directly.

The composition root is the intentional place where this Minecraft/NeoForge build chooses its concrete TC4 implementation.

## Capability support

The TC4 profile declares all **36** `HostProfile.Feature` values exactly once. The boundary checker verifies the code boundary; GameTests verify the feature-support matrix against the adapter declaration.

Support levels mean:

- `NATIVE` — provided directly by the selected host;
- `ADAPTED` — implemented through the adapter;
- `PARTIAL` — only part of the capability is available;
- `UNSUPPORTED` — no implementation is provided.

Known non-full entries in this track are explicit:

- `TAINT_ORIGINS` — `UNSUPPORTED`: no host taint-origin registration or propagation adapter is implemented.
- `OPTIONAL_ENCHANTMENT_INTEGRATION` — `PARTIAL`: Thaumic Tinkerer enchantment costs are registered only when that optional mod is installed.
- `RESEARCH_STAGES` — `PARTIAL`: only one page group is supported; multi-stage research definitions are rejected.

Other declared features are marked `ADAPTED` in the TC4 profile. These declarations describe this adapter only; they do not imply that another magic host has the same capabilities.

## Verification

The 26.1.2 port passed the host-agnostic API boundary check, pinned Thaumcraft preparation/build, add-on compilation, artifact provenance, and generated-resource freshness checks.

CI run [#472](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/41375528543) passed **111/111 GameTests** with Luxuria enabled and **111/111** with Luxuria disabled. The test matrix includes the selected host profile, semantic content resolution, recipe/research registration, wand and warp ports, entity classification, and Wrath Cage Essentia bridging.

Headless GameTests do not replace launching the client or manually checking rendering, tooltips, and in-world interactions. Those checks remain a separate release-readiness gate.

## Non-goals

- No attempt is made to abstract Minecraft or NeoForge out of the mod runtime.
- No shared JAR is produced for different Minecraft versions.
- No second magic-host adapter is included in this port.
- The Thaumaturge compatibility audit is not a production adapter or proof of multi-host support.
