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

The narrow interfaces currently cover aspect services, item properties and factories, entity aspects and classification, morph tools, block-entity construction, wand inspection, full typed wand Vis reads/writes plus separate item-backed real-Vis storage, focus upgrade/Vis operations, typed wand-parts mutation, Essentia source extraction, accessory slots, taint block services, enchantment effects, scribing operations, warp, wand/trigger registration, optional enchantment integration, and creative-content contribution.

These contracts may expose Minecraft/NeoForge runtime types required by the 1.21.1 implementation. They must not expose Thaumcraft types or concrete adapter classes.

### TC4 implementation — `integration/tc4/`

The concrete layer owns conversions between semantic identifiers and TC4 values, TC4 aspect definitions and aspect-tag/scan-target registration, recipe/research registration, wand and Vis calls, Baubles, taint damage, special mining, Thaumcraft entity classification, TC4 item properties/factories, and warp-pool mapping.

Core Minecraft gameplay consumers remain in the mod's regular source packages; direct Thaumcraft API references are confined to `integration/tc4/`, with `ForbiddenMagic` retaining the single composition-root import that installs the selected adapter. `WandMaterialCatalog` owns semantic rod/cap registration data, while `WandTriggerCatalog` owns Hellfire trigger blocks, research, aspect, and Vis cost. `Tc4WandAdapter` now only maps material definitions into TC4 registries; `Tc4WandUpdateAdapter` isolates `RodUpdate` callback construction, and `Tc4ArsMagicaManaUpdate` isolates reflective access to optional Ars Magica mana APIs. Shared rod behaviors consume the typed `HostWandVisServices` and wand-parts contracts. Native creative wand assembly and rod-part mutation are provided through the focused `HostCreativeContentServices` and `HostWandPartsServices` capabilities. `Tc4BlinkFocusItem` is the TC4 `FocusItem` integration wrapper. Targeting, destination calculation and teleport/combat behavior live in `core/wand/BlinkFocusBehavior`; upgrade progression and semantic Vis-cost definitions live in `core/wand/BlinkFocusRules`. Focus upgrade inspection and Vis payment use `HostFocusServices`; `Tc4FocusAdapter` converts semantic upgrades and Vis costs to TC4 `FocusUpgradeType` and `AspectList` values, while `FocusItem` and `WandItem` lifecycle calls stay in `integration/tc4/`. `Tc4WrathCageBlockEntity` in `integration/tc4/` remains the TC4 block-entity/EssentiaSource/EssentiaTransport callback wrapper. The complete logical cage state (attunement, storage, fuel, spawn delay, and Sloth mode) lives in `core/block/WrathCageState`, which also owns spawn eligibility, fuel-channel consumption, and spawn-delay advancement; channel capacities, routing, extraction planning, and storage snapshots live in `core/block/WrathCageStorage`. The TC4 callback wrapper converts incoming `Aspect` values to `ContentId` and delegates channel selection and storage mutations to the core. The tick wrapper delegates the spawn gate, fuel-channel selection/application, and spawn-delay advancement to `WrathCageState`. Complete spawn-round orchestration lives in `core/block/WrathCageSpawnBehavior` and uses Minecraft `ServerLevel`, entity registry, entity creation, placement, and collision APIs directly; this is intentional because Minecraft remains the runtime target rather than an abstracted host. Physical pulls from neighboring sources are performed through `HostEssentiaTransportServices` by `Tc4EssentiaTransportAdapter`, which alone checks `EssentiaTransport` and output direction. The block entity maps the complete core snapshot to the unchanged legacy `MobId`, `Special`, `Wrath`, `Sloth`, `Fuel`, `Delay`, `Slothful`, and `Mode` keys and still owns `ValueInput`/`ValueOutput`, callback interfaces, and entity spawning. Compatibility facades such as `FmWands` delegate Witchwood mana rules to the shared core rather than duplicating formulas. The former broad `HostBootstrapServices` contract is split into independent wand-registration, optional-enchantment, and creative-content capabilities.

## Current capability status

The 1.21.1 `Tc4HostAdapter` declares every `HostProfile.Feature`. The currently adapted capabilities include:

- Aspect registration, diagnostics, presentation, and scan targets.
- Semantic recipe and research catalog registration for the supported recipe/page forms, including sibling links and item, entity, and aspect unlock triggers.
- Warp pools and sticky warp mapping.
- Morph-tool area mining and cycle sound.
- Wand inspection and typed wand Vis reads/writes, with item-backed real-Vis writes kept as a distinct operation.
- Semantic wand rods/caps, update behavior, trigger rules, typed part mutation, Blink Focus progression and cost profiles, all materialized through native TC4 registration/lifecycle adapters.
- Bounded Essentia storage and extraction policy, with native persistence and physical transport calls isolated in the TC4 block-entity/transport adapters.
- Host-native creative-tab contributions through the dedicated creative-content capability (currently catalogued wand samples).
- Baubles accessory lookup and slot assignment.
- Taint material classification, taint-shovel behavior, and special-mining/taint-damage hooks.
- Scribing research difficulty, primal-aspect selection, aspect-pool updates, and spent scribing-tool creation.
- Wrath Cage block-entity construction.
- Entity-to-aspect catalog and host entity classification; Wrath Cage aspect lookup and mob-imprint boss checks consume semantic IDs from these services instead of duplicating TC4 Aspect and boss registries.
- Host-specific item properties and factories. TC4 item shells that implement `ScribingToolsItem`, `Bauble`, `RunicArmor`, `VisAmuletItem`, or `WarpingGear` live in `integration/tc4`; the shared registration API receives only Minecraft `Item` instances.

Known limits remain explicit:

| Capability | Current declaration | Limitation |
|---|---|---|
| Research stages | `PARTIAL` | The pinned adapter accepts one page group; multi-stage definitions are rejected |
| Optional enchantment integration | `PARTIAL` | Thaumic Tinkerer is optional and its current bridge uses reflective API discovery; missing or incompatible APIs leave the integration disabled with a warning |
| Taint origins | `UNSUPPORTED` | No taint-origin registration or propagation adapter is implemented |

Every other declared capability is backed by an implemented runtime capability and currently reports `ADAPTED`. Native Thaumcraft objects and callbacks remain inside the TC4 adapter by design; their presence there is not a portability gap.

## Validation

The 1.21.1 workflow builds the pinned Thaumcraft host and addon, verifies artifact provenance, runs GameTests, and uploads the resulting JAR. The architecture boundary checker rejects Thaumcraft dependencies in `core/`, `integration/api/`, and `integration/runtime/`, and checks all production sources outside `integration/tc4/` for direct host imports. The only production exception is the single `Tc4HostAdapter` import in the `ForbiddenMagic` composition root.

The GameTest `hostAdapterDeclaresThe1_21_1CapabilityProfile` asserts the selected host/Minecraft versions, full feature coverage, and representative `ADAPTED`, `PARTIAL`, and `UNSUPPORTED` declarations.

A green headless CI run verifies compilation and GameTest behavior; it does not replace manual client checks for rendering, tooltips, wand/focus interactions, and in-world visuals before release.

## Rules for future changes

1. Put shared definitions and semantics in `core/`; resolve them to host objects only inside `integration/tc4/`.
2. Prefer a narrow runtime capability over a growing aggregate host interface.
3. Keep host-specific registrations and callbacks inside `integration/tc4/`, and expose only the operations actually consumed through narrow runtime capabilities.
4. Update the profile and its GameTest whenever support changes.
5. Keep MC/NeoForge version tracks and binaries separate.
6. Preserve legacy gameplay behavior while moving implementation behind the adapter; do not silently replace mechanics with approximations.
