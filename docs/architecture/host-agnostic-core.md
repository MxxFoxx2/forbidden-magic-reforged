# Host-agnostic core on Minecraft 26.1.2

**Target:** Minecraft 26.1.2 / NeoForge 26.1.2.122 / Thaumcraft 4: Reforged 1.1.2  
**Port branch:** `architecture/26.1.2-host-agnostic`  
**Status:** Initial core and contract types are present; TC4 adapter and runtime wiring are still in progress.

## Architectural intent

Forbidden Magic is a Minecraft mod. Minecraft and the selected loader are first-class parts of its runtime architecture, not dependencies to hide behind a generic abstraction.

The portability boundary is the **magic host**, not Minecraft:

- Shared content definitions and gameplay rules use Minecraft-native types where gameplay requires them.
- Thaumcraft-specific aspects, recipe/research registries, wand APIs, Vis storage, warp, Essentia, taint classification, and accessory integrations belong behind the TC4 adapter.
- Minecraft/NeoForge version differences are handled by separate release tracks and version-specific code where the APIs actually differ.
- No single JAR or common Minecraft abstraction layer is intended to cover 26.3, 26.1.2, and 1.21.1.

## Layers

### Shared Forbidden Magic core

`core/model` contains semantic `ContentId` and symbolic references to content owned by the selected magic host.

`core/aspect`, `core/recipe`, and `core/research` describe Forbidden Magic data without registering it directly in a host registry.

`core/wand` and `core/warp` contain reusable gameplay rules. Their access ports still use Minecraft types such as `ItemStack`, `Player`, and `ServerPlayer` where the game interaction requires them.

### Magic-host contract

`integration/api` defines `HostAdapter`, `HostProfile`, and the adapter registry. `integration/runtime` holds focused Minecraft-native capabilities for host-provided behavior.

### Version-specific TC4 integration

The 26.1.2 track will implement these contracts against Thaumcraft 4: Reforged 1.1.2 and the 26.1.2 NeoForge API in `integration/tc4`. It remains a separate build from 26.3 and 1.21.1.

## Porting gates

1. Install exactly one TC4 adapter at the mod composition root.
2. Route aspect, recipe, and research definitions through the adapter.
3. Move TC4 runtime calls out of common gameplay classes into focused adapter methods.
4. Declare every host feature in `HostProfile`; unsupported or partial behavior must remain explicit.
5. Add GameTests for each migrated capability and for behavior preserved from the 26.1.2 baseline.
6. Pass host preparation/build, addon build, provenance/resource checks, and both Luxuria GameTest configurations before considering the port complete.

The initial port commit only adds the shared model/catalog layer and adapter contracts. It does not yet make the 26.1.2 runtime use the adapter.
