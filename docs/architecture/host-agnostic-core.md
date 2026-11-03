# Host-agnostic core architecture — Minecraft 1.21.1

**Track:** `architecture/1.21.1-host-agnostic`  
**Runtime target:** Minecraft 1.21.1 / NeoForge 21.1.256 / Thaumcraft 4: Reforged 1.1.2 / Java 21  
**Status:** The 1.21.1 host-agnostic architecture migration is complete for the implemented feature set. Direct Thaumcraft API usage is isolated in `integration/tc4/`; remaining limitations are explicitly recorded in the capability table and test suite.

## Purpose and non-goals

The portability boundary is the **magic host**, not Minecraft. Shared definitions and behavior contracts should not depend on Thaumcraft's registry, wand, aspect, research, recipe, Baubles, or Essentia classes. Minecraft and NeoForge remain first-class runtime dependencies.

The 1.21.1 runtime is a separate track. Its adapter uses the 1.21.1 `ResourceLocation`, damage, particle, item-property, block-entity, and Thaumcraft APIs. It is not source- or binary-compatible with the newer 26.x tracks merely by changing version constants.

This branch does not produce a universal JAR across Minecraft versions, does not alter `main` or `mc/26.1.2`, and does not claim a second production magic-host adapter.

## Layers

### Shared semantic model — `core/`

The shared core owns:

- `ContentId` and symbolic host content references.
- Aspect, recipe, and research definitions/catalogs.
  `FmRecipes` and `FmResearch` are thin registration facades; their full definitions live in `ForbiddenMagicRecipeCatalog` and `ForbiddenMagicResearchCatalog`.
- Shared wand behavior rules and narrow access contracts, including Blink Focus targeting, teleport effects, upgrade progression/cost profiles, and Witchwood mana conversion rates.
- Shared Wrath Cage logical state (attunement, storage, fuel, spawn delay, and Sloth mode), spawn eligibility and fuel selection/application, spawn-delay countdown rules, complete spawn-round orchestration, player-range gating, vanilla entity resolution/creation, per-round attempt limits, nearby-entity caps, placement and collision checks, bounded three-channel Essentia storage, semantic aspect-to-channel routing, extraction eligibility, display-mode selection, and immutable state snapshot/restore.
- Semantic warp kinds and access contracts.

The core may use Minecraft runtime types where behavior necessarily operates on Minecraft objects, such as `ItemStack`, entities, and levels. It must not import Thaumcraft or NeoForge implementation APIs, or refer to the concrete TC4 adapter.

### Adapter contract — `integration/api/`

- `HostAdapter` describes the selected host's catalog registration, diagnostics, bootstrap, and profile; shared commands and tests retrieve registry diagnostics through this neutral contract. `HostBlockEntityServices` exposes only the Minecraft `BlockEntity` base type and typed lifecycle/interaction operations; the concrete `Tc4WrathCageBlockEntity` implementation stays inside `integration/tc4/`.
- `HostProfile` declares every capability as `NATIVE`, `ADAPTED`, `PARTIAL`, or `UNSUPPORTED`.
- `HostAdapterRegistry` installs the selected adapter once and exposes typed capability lookups.

`ForbiddenMagic` is the composition root. It installs `Tc4HostAdapter.INSTANCE` before shared aspect catalog registration and before item registration causes host item properties/factories to be resolved.

### Runtime capabilities — `integration/runtime/`

The narrow interfaces currently cover aspect services, item properties and factories, entity aspects and classification, morph tools, block-entity construction, wand inspection, full typed wand Vis reads/writes plus separate item-backed real-Vis storage, focus upgrade/Vis operations, typed wand-parts mutation, Essentia source extraction, accessory slots, taint block and taint-origin services, enchantment effects, scribing operations, warp, wand/trigger registration, optional enchantment integration, and creative-content contribution.

These contracts may expose Minecraft/NeoForge runtime types required by the 1.21.1 implementation. They must not expose Thaumcraft types or concrete adapter classes.

### TC4 implementation — `integration/tc4/`

The concrete layer owns conversions between semantic identifiers and TC4 values, TC4 aspect definitions and aspect-tag/scan-target registration, recipe/research registration, wand and Vis calls, Baubles, taint materials/origins and taint spread, taint damage, special mining, Thaumcraft entity classification, TC4 item properties/factories, and warp-pool mapping.

Core Minecraft gameplay consumers remain in the mod's regular source packages; direct Thaumcraft API references are confined to `integration/tc4/`, with `ForbiddenMagic` retaining the single composition-root import that installs the selected adapter. `WandMaterialCatalog` owns semantic rod/cap registration data, while `WandTriggerCatalog` owns Hellfire trigger blocks, research, aspect, and Vis cost. `Tc4WandAdapter` now only maps material definitions into TC4 registries; `Tc4WandUpdateAdapter` isolates `RodUpdate` callback construction, and `Tc4ArsMagicaManaUpdate` isolates reflective access to optional Ars Magica mana APIs. Shared rod behaviors consume the typed `HostWandVisServices` and wand-parts contracts. Native creative wand assembly and rod-part mutation are provided through the focused `HostCreativeContentServices` and `HostWandPartsServices` capabilities. `Tc4BlinkFocusItem` is the TC4 `FocusItem` integration wrapper. Targeting, destination calculation and teleport/combat behavior live in `core/wand/BlinkFocusBehavior`; upgrade progression and semantic Vis-cost definitions live in `core/wand/BlinkFocusRules`. Focus upgrade inspection and Vis payment use `HostFocusServices`; `Tc4FocusAdapter` converts semantic upgrades and Vis costs to TC4 `FocusUpgradeType` and `AspectList` values, while `FocusItem` and `WandItem` lifecycle calls stay in `integration/tc4/`. `Tc4WrathCageBlockEntity` in `integration/tc4/` remains the TC4 block-entity/EssentiaSource/EssentiaTransport callback wrapper. The complete logical cage state (attunement, storage, fuel, spawn delay, and Sloth mode) lives in `core/block/WrathCageState`, which also owns spawn eligibility, fuel-channel consumption, and spawn-delay advancement; channel capacities, routing, extraction planning, and storage snapshots live in `core/block/WrathCageStorage`. The TC4 callback wrapper converts incoming `Aspect` values to `ContentId` and delegates channel selection and storage mutations to the core. The tick wrapper delegates the spawn gate, fuel-channel selection/application, and spawn-delay advancement to `WrathCageState`. Complete spawn-round orchestration lives in `core/block/WrathCageSpawnBehavior` and uses Minecraft `ServerLevel`, entity registry, entity creation, placement, and collision APIs directly; this is intentional because Minecraft remains the runtime target rather than an abstracted host. Physical pulls from neighboring sources are performed through `HostEssentiaTransportServices` by `Tc4EssentiaTransportAdapter`, which alone checks `EssentiaTransport` and output direction. The block entity maps the complete core snapshot to the unchanged legacy `MobId`, `Special`, `Wrath`, `Sloth`, `Fuel`, `Delay`, `Slothful`, and `Mode` keys and still owns `ValueInput`/`ValueOutput`, callback interfaces, and entity spawning. Compatibility facades such as `FmWands` delegate Witchwood mana rules to the shared core rather than duplicating formulas. The former broad `HostBootstrapServices` contract is split into independent wand-registration, optional-enchantment, and creative-content capabilities.

## Current capability status

The 1.21.1 `Tc4HostAdapter` declares every `HostProfile.Feature`. The currently adapted capabilities include:

- Aspect registration, diagnostics, presentation, and scan targets.
- Semantic recipe and research catalog registration, including multi-stage definitions lowered to sequential native research entries, stage-specific prerequisites/pages/warp, sibling links, and item, entity, and aspect unlock triggers.
- Warp pools and sticky warp mapping.
- Morph-tool area mining and cycle sound.
- Wand inspection and typed wand Vis reads/writes, with item-backed real-Vis writes kept as a distinct operation.
- Semantic wand rods/caps, update behavior, trigger rules, typed part mutation, Blink Focus progression and cost profiles, all materialized through native TC4 registration/lifecycle adapters.
- Bounded Essentia storage and extraction policy, with native persistence and physical transport calls isolated in the TC4 block-entity/transport adapters.
- Host-native creative-tab contributions through the dedicated creative-content capability (currently catalogued wand samples).
- Baubles accessory lookup and slot assignment.
- Taint material classification, taint-shovel behavior, opt-in taint-origin registration/adjacent fibre propagation, and special-mining/taint-damage hooks.
- Optional Thaumic Tinkerer enchantment-cost integration through a typed, isolated adapter compiled against the pinned public 1.21.1 API as `compileOnly`.
- Scribing research difficulty, primal-aspect selection, aspect-pool updates, and spent scribing-tool creation.
- Wrath Cage block-entity construction.
- Entity-to-aspect catalog and host entity classification; Wrath Cage aspect lookup and mob-imprint boss checks consume semantic IDs from these services instead of duplicating TC4 Aspect and boss registries.
- Host-specific item properties and factories. TC4 item shells that implement `ScribingToolsItem`, `Bauble`, `RunicArmor`, `VisAmuletItem`, or `WarpingGear` live in `integration/tc4`; the shared registration API receives only Minecraft `Item` instances.

The three previously recorded TC4 gaps now have explicit implementations:

- **Research stages:** the adapter lowers each semantic stage to a native TC4 `ResearchItem`. Intermediate stages receive deterministic adapter-owned keys, each requires the previous stage plus its own prerequisites, and the final stage retains the definition's canonical key. Definition note cost is charged on the first stage only. Later zero-cost entries auto-complete only after their previous stage and stage-specific prerequisites are complete, revealing their own pages and applying their warp without charging a second note. Intermediate entries are placed before the canonical entry while avoiding occupied category coordinates.
- **Thaumic Tinkerer:** the add-on remains optional, but the bridge is now linked against the pinned 1.21.1 public API at compile time. Gradle downloads the published `1.1-1.21.1` API JAR into `build/`, verifies SHA-256 `f38c4ec9791b96a195953ed7cdfd59324f978c0bae4f830f7f2ff59e9aed9736`, and adds it as `compileOnly`; the runtime JAR is not bundled. All eight `Data` records are materialized before the public map is changed, and writes are rolled back if the map rejects a mutation. The TT-linked adapter is isolated in its own class and is invoked only when `thaumictinkerer` is loaded; no TT classes need to resolve for installations without the mod. Disabled enchantments and an absent mod are no-ops.
- **Taint origins:** `HostTaintOriginServices` exposes origin block registration, unregistration, classification, and an adjacent propagation operation. A local patch to the pinned TC4 host makes registered origins participate in taint adjacency and delegates fibre placement to the host's `TaintSpread`; the target biome is marked tainted only after fibre placement succeeds. The caller owns the origin's tick cadence.

The port does not automatically register an existing Forbidden Magic block as a source: the legacy source audit did not establish that any FM block acted as an independent taint origin. The new API is opt-in so that future content can register an origin without silently changing existing gameplay. Native Thaumcraft objects and callbacks remain inside the TC4 adapter or the pinned host patch by design; their presence there is not a portability gap.

## Validation

The 1.21.1 workflow builds the pinned Thaumcraft host and addon, verifies artifact provenance, runs GameTests, and uploads the resulting JAR. The build fetches the pinned Thaumic Tinkerer API artifact and verifies its SHA-256 before compiling the optional typed adapter; the `verifyTinkererCompileOnly` task also fails if any `thaumictinkerer/` classes appear in the produced Forbidden Magic JAR. The architecture boundary checker rejects Thaumcraft dependencies in `core/`, `integration/api/`, and `integration/runtime/`, and checks all production sources outside `integration/tc4/` for direct host imports. The only production exception is the single `Tc4HostAdapter` import in the `ForbiddenMagic` composition root.

`ArchitectureCapabilityGameTests` checks the three former gaps at runtime: stage lowering, delayed continuation-stage advancement until prerequisites are satisfied, the adapted capability declarations, and origin registration plus a real adjacent fibre spread through the TC4 host engine. `EnchantmentGameTests.tinkererIntegrationIsOptional` verifies no-op behavior without Thaumic Tinkerer, eight registrations when it is present, and idempotent repeated registration. The host-profile test and existing profile audit continue to require a non-empty explanation for every declared feature. Catalog registration, scan-target registration, diagnostics, and runtime bootstrap are intentionally provided by the base `HostAdapter`; the event-time services are exposed through the narrow `Host*Services` interfaces. The test also checks the mapping between those typed services and the corresponding profile features.

A green headless CI run verifies compilation and GameTest behavior; it does not replace manual client checks for rendering, tooltips, wand/focus interactions, and in-world visuals before release.

## Rules for future changes

1. Put shared definitions and semantics in `core/`; resolve them to host objects only inside `integration/tc4/`.
2. Prefer a narrow runtime capability over a growing aggregate host interface.
3. Keep host-specific registrations and callbacks inside `integration/tc4/`, and expose only the operations actually consumed through narrow runtime capabilities.
4. Update the profile and its GameTest whenever support changes.
5. Keep MC/NeoForge version tracks and binaries separate.
6. Preserve legacy gameplay behavior while moving implementation behind the adapter; do not silently replace mechanics with approximations.
