# Thaumaturge compatibility audit — 26.1.2

**Status:** source/API audit only. No Thaumaturge adapter or dependency has been added to production code.

## Snapshot and version gate

The audit is pinned to Thaumaturge `main` commit [`d91b0594421a802d5df64474a215bcd78ec9de29`](https://github.com/Leclowndu93150/Thaumaturge/commit/d91b0594421a802d5df64474a215bcd78ec9de29). Its build metadata declares mod version `1.0.3`, Minecraft `26.1.2`, and NeoForge `26.1.2.122`.

The Forbidden Magic architecture branch audited here is on Minecraft `26.3` / NeoForge `26.3.0.57-beta`. These are different Minecraft/NeoForge targets. A Thaumaturge adapter cannot be compiled and verified inside the current 26.3 build merely by adding another integration package. It needs a separate compatible release track, or a later Thaumaturge release targeting 26.3. Do not mix both hosts into one binary or pretend that a source-level API match proves runtime compatibility.

## API correspondence

| Forbidden Magic capability / current port | Thaumaturge API observed | Assessment | Required adaptation or gap |
|---|---|---|---|
| Aspect definitions and semantic aspect IDs | `IAspect.REGISTRY_KEY`, `AspectList`, `Aspects.resolve`; datapack registry in `TTDatapackRegistries` | **ADAPTED** | Translate `ContentId` to registry keys/holders. The seven FM aspect definitions can be expressed as Thaumaturge aspect registry data. Do not assume TC4 singleton tags or registry order. |
| Item/entity aspect derivation | `AspectDataMaps.BASE_ASPECTS`, `ENTITY_ASPECTS`, `IAspectRecipeContributor` | **ADAPTED** | Emit data-map entries for items/entities and register recipe contributors where derivation needs code. This is not the TC4 `AddonAspects` registrar model. |
| Aspect costs in shared recipes/research | Immutable, holder-based `AspectList` with `add`, `remove`, `amountOf`, and `merge` | **ADAPTED** | Convert the shared cost model into registry holders and the host's immutable list. Keep `ContentId` in core; do not leak `Holder<IAspect>` into it. |
| Research categories and entries | Datapack registries `IResearchCategory.REGISTRY_KEY` and `IResearchEntry.REGISTRY_KEY`; each entry has ordered `IResearchStage` values | **ADAPTED / PARTIAL** | Multiple stages, prerequisites, item obtain/craft requirements, knowledge rewards/costs, and stage warp are expressible. The host model is not a TC4 page array: FM text-page grouping, icon/flag semantics, and recipe-page display need explicit mapping and tests. |
| Arcane recipes | `IArcaneRecipe`, `IArcaneCraftingInput`, `ArcaneCraftCost`, `ResearchGated` | **PARTIAL** | The host separates aura/wand `visCost` from crystal `AspectList` cost. Current `RecipeDefinition.ArcaneShaped/ArcaneShapeless` expose only one aspect cost. Extend the neutral model before implementing this adapter; do not guess the missing vis cost. |
| Infusion recipes | `IInfusionRecipe` and `InfusionCraftingTransaction` | **ADAPTED** | Output, aspects, instability, ingredients, and research gate have counterparts. The shared definition currently separates the central ingredient from components; the adapter must combine them according to host recipe semantics. Verify remainders and transaction behavior with GameTests. |
| Crucible recipes | Public `CrucibleEvent` lifecycle events were found | **UNRESOLVED** | No public add-on crucible recipe contract/registration API was confirmed in this audit. An event hook is not proof that custom recipes can be registered. Inspect recipe loading/serializer extension points before declaring support. |
| Infusion enchantments | Public `InfusionEnchantment` is an enum of host-defined values | **PARTIAL** | There is no confirmed public extension point for adding new enum values. FM effects may need ordinary enchantment registrations or a separate custom recipe/effect implementation. |
| Wand rod and cap definitions | Public `WandRod`, `WandCap` registry keys; `IWandRodOnUpdate`, `IWandRodOnAssemble`, `IWandVisStorage`; host registers rods/caps with `DeferredRegister` | **ADAPTED / PARTIAL** | A compatible addon can register its own rod/cap values and update callbacks. The matching rod/cap item wrappers and assembly recipes are separate registrations and still need a prototype. |
| Wand vis storage | `WandAccess`, `WandVis`, `IWandVisStorage` | **ADAPTED** | Amounts are explicitly in centivis (100 per vis). `WandAccess.withVis` returns a copied stack, not an in-place mutation. Custom FM rods can provide `IWandVisStorage` for callback-side mutation; test copy/replace behavior and capacity rules. |
| Wand part lookup/replacement | Public `WandAccess` exposes vis only; `WandRod`/`WandCap` are registry values | **PARTIAL — BLOCKER** | No public API to read the rod/cap currently installed in an arbitrary wand or replace a wand's rod was found. This blocks a direct port of `WandPartsAccess.setRod` and the Profane → Profaned transition. An assembly marker component could help identify FM rods, but it does not solve safe part replacement. Do not reach into internal wand components without explicitly accepting a version-fragile adapter. |
| Focus system / Blink Focus | `FocusTier.DATA_MAP`, datapack `SpellPart`, `SpellBehaviorType.REGISTRY_KEY`, `Spells.spellOf/setSpell/analyze/cast` | **ADAPTED, WITH GAMEPLAY REDESIGN** | Thaumaturge has a real spell/focus framework. Blink can become an FM focus item with its own focus-tier data and spell part/behavior. TC4 `FocusUpgradeType`, rank progression, and upgrade storage are not a 1:1 match; Hellfire/Pandemonium should be separate spell settings/parts or explicitly redesigned. |
| Warp pools | `IPlayerWarp`, `WarpHelper`, `ItemWarp`, `WarpType` | **PARTIAL** | Host pools are `PERMANENT`, `NORMAL`, and `TEMPORARY`; FM core currently distinguishes `PERMANENT`, `STICKY`, and `TEMPORARY`. `STICKY → NORMAL` is only a candidate mapping. Verify cleansing, decay, and acquisition behavior before accepting it. |
| Taint query and origins | `TaintApi.isTainted`, ecological pressure/seed operations, `ITaintOrigin` | **ADAPTED / PARTIAL** | Taint queries can be routed through the host API. Its documented tainted-land semantics include biome tags and chunk ecological pressure; compare that with each TC4 behavior before assuming identical results. Custom origins can implement the host interface in the adapter layer. |
| Essentia storage and transport | `IEssentiaStorage`, `IEssentiaTransport`, `EssentiaAccess.transport` | **ADAPTED** | The host exposes both storage and transport APIs, but storage operations use `TransactionContext`. Map the existing `WrathCageBlockEntity` callback port through a transactional bridge and test simulate/commit, side rules, and partial transfers. |
| Host-specific mob/block classification | Host entity/block types and tags; no exact public counterpart for every TC4 marker used by FM was established | **UNRESOLVED** | Replace direct checks such as TC4 tainted-mob/boss classes with explicit host predicates/tags. Each replacement needs a parity test; do not infer equivalence from names. |
| Focus Treasure / Focus Potency bonuses | No 1:1 public wand API counterpart was identified | **UNRESOLVED** | Current drop/damage formulas read TC4 focus upgrades. Do not substitute total spell complexity or power without defining and testing a deliberate gameplay mapping. |

## Architectural consequences

1. Keep `core` expressed in semantic IDs and host-neutral behavior ports. Thaumaturge types such as `Holder<IAspect>`, `WandVis`, `IResearchEntry`, `SpellPart`, and `TransactionContext` belong only in `integration/thaumaturge`.
2. A future adapter should be independent of `integration/tc4`. Do not create a common superclass or import either host API into gameplay.
3. Extend the shared arcane recipe model to represent both aura/wand vis and crystal essentia costs before implementing the Thaumaturge recipe adapter.
4. Treat Profane rod conversion, Sticky warp, infusion enchantment registration, and the two focus-upgrade bonuses as explicit blockers until the relevant semantics/API are solved.
5. Add host profile declarations only when backed by implementation and tests. A planned mapping is not `NATIVE` or `ADAPTED` support.

## Version-specific implementation order

1. Create a separate Minecraft `26.1.2` release/build track, pinned to an exact Thaumaturge artifact/API version. Keep the current 26.3 track unchanged.
2. Prototype a minimal `integration/thaumaturge` adapter for aspect IDs, item/entity aspect maps, and a read/write vis round-trip. Run a build against the exact host dependency.
3. Add custom rod/cap registration and a custom spell focus; test assembly, update callbacks, centivis persistence, and server/client sync.
4. Port research and arcane/infusion recipes, first extending the neutral recipe model for separate vis and crystal costs.
5. Port taint/warp/essentia behind ports and record any non-equivalent semantics in the fidelity ledger.
6. Finish with GameTests and an in-client pass. No second-host support claim before the matching version build and tests pass.

## Source snapshot and license handling

This report studies only the public API and public build metadata at the commit pinned above. It does not copy Thaumaturge implementation code, assets, or binaries. The repository's current license permits reading/studying and personal use but restricts publishing Thaumaturge itself or modified versions. This audit does not make a legal determination about every possible separately-authored add-on distribution; obtain written permission where the license's scope is uncertain before distributing anything that incorporates host material.

### References

- [Thaumaturge audited commit](https://github.com/Leclowndu93150/Thaumaturge/commit/d91b0594421a802d5df64474a215bcd78ec9de29)
- [Build/version metadata](https://github.com/Leclowndu93150/Thaumaturge/blob/d91b0594421a802d5df64474a215bcd78ec9de29/gradle.properties)
- [License](https://github.com/Leclowndu93150/Thaumaturge/blob/d91b0594421a802d5df64474a215bcd78ec9de29/LICENSE)
- [Datapack registry declarations](https://github.com/Leclowndu93150/Thaumaturge/blob/d91b0594421a802d5df64474a215bcd78ec9de29/src/main/java/com/leclowndu93150/thaumaturge/registry/TTDatapackRegistries.java)
- [WandAccess](https://github.com/Leclowndu93150/Thaumaturge/blob/d91b0594421a802d5df64474a215bcd78ec9de29/src/main/java/com/leclowndu93150/thaumaturge/api/wands/WandAccess.java)
- [Spells](https://github.com/Leclowndu93150/Thaumaturge/blob/d91b0594421a802d5df64474a215bcd78ec9de29/src/main/java/com/leclowndu93150/thaumaturge/api/spell/Spells.java)
- [TaintApi](https://github.com/Leclowndu93150/Thaumaturge/blob/d91b0594421a802d5df64474a215bcd78ec9de29/src/main/java/com/leclowndu93150/thaumaturge/api/taint/TaintApi.java)
- [IArcaneRecipe](https://github.com/Leclowndu93150/Thaumaturge/blob/d91b0594421a802d5df64474a215bcd78ec9de29/src/main/java/com/leclowndu93150/thaumaturge/api/recipe/IArcaneRecipe.java)
- [IResearchEntry](https://github.com/Leclowndu93150/Thaumaturge/blob/d91b0594421a802d5df64474a215bcd78ec9de29/src/main/java/com/leclowndu93150/thaumaturge/api/research/IResearchEntry.java)
