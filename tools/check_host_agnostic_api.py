from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
BOUNDARIES = (
    ROOT / "src/main/java/dev/visfox/forbiddenreforged/core/model",
    ROOT / "src/main/java/dev/visfox/forbiddenreforged/core/aspect",
    ROOT / "src/main/java/dev/visfox/forbiddenreforged/core/recipe",
    ROOT / "src/main/java/dev/visfox/forbiddenreforged/core/research",
    ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/api",
)
HOST_NEUTRAL_GAMEPLAY = (
    ROOT / "src/main/java/dev/visfox/forbiddenreforged/WrathCageBlockEntity.java",
    ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmSpawnerMobs.java",
    ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/runtime/HostEntityAspectServices.java",
    ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/runtime/HostEntityClassificationServices.java",
    ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmWandTriggers.java",
    ROOT / "src/main/java/dev/visfox/forbiddenreforged/core/wand",
)
FORBIDDEN = (
    "import thaumcraft.",
    "import com.leclowndu93150.thaumaturge.",
    "import net.minecraft.",
    "import net.neoforged.",
)
HOST_FORBIDDEN = (
    "thaumcraft.",
    "com.leclowndu93150.thaumaturge.",
    "TcBlockEntity",
    "AspectSource",
    "EssentiaTransport",
    "thaumcraft:",
    "com.leclowndu93150.thaumaturge:",
)

WANDS_USAGE_BOUNDARY = (
    ROOT / "src/main/java",
)


def main() -> int:
    failures = []
    checked = 0

    for boundary in BOUNDARIES:
        if not boundary.is_dir():
            failures.append(f"Missing architecture boundary: {boundary.relative_to(ROOT)}")
            continue

        for source in boundary.rglob("*.java"):
            checked += 1
            content = source.read_text(encoding="utf-8")
            for forbidden in FORBIDDEN:
                if forbidden in content:
                    failures.append(
                        f"{source.relative_to(ROOT)} imports forbidden dependency: {forbidden}"
                    )

    for boundary in HOST_NEUTRAL_GAMEPLAY:
        if not boundary.exists():
            failures.append(f"Missing host-neutral gameplay boundary: {boundary.relative_to(ROOT)}")
            continue
        sources = boundary.rglob("*.java") if boundary.is_dir() else (boundary,)
        for source in sources:
            checked += 1
            content = source.read_text(encoding="utf-8")
            for forbidden in HOST_FORBIDDEN:
                if forbidden in content:
                    failures.append(
                        f"{source.relative_to(ROOT)} crosses host boundary: {forbidden}"
                    )

    for source_root in WANDS_USAGE_BOUNDARY:
        for source in source_root.rglob("*.java"):
            if "integration/tc4" in source.as_posix():
                continue
            content = source.read_text(encoding="utf-8")
            if re.search(r"\bWands\b", content):
                failures.append(
                    f"{source.relative_to(ROOT)} accesses the TC4 wand API outside integration/tc4"
                )

            for host_type in ("TaintedMob", "ThaumcraftBoss"):
                if re.search(rf"\b{host_type}\b", content):
                    failures.append(
                        f"{source.relative_to(ROOT)} references TC4 entity type {host_type} outside integration/tc4"
                    )

    event_boundaries = (
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmDrops.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmToolEvents.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmEnchantmentEffects.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmSetup.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/ScribeWellEvents.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmItems.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmCommands.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/TaintShovelItem.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/RidingCropItem.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/MorphToolItem.java",
    )
    for source in event_boundaries:
        content = source.read_text(encoding="utf-8")
        if "thaumcraft." in content:
            failures.append(
                f"{source.relative_to(ROOT)} references TC4 directly instead of using integration/tc4"
            )

    core_root = ROOT / "src/main/java/dev/visfox/forbiddenreforged/core"
    for source in core_root.rglob("*.java"):
        content = source.read_text(encoding="utf-8")
        for host_namespace in ("thaumcraft:", "com.leclowndu93150.thaumaturge:"):
            if host_namespace in content:
                failures.append(
                    f"{source.relative_to(ROOT)} hard-codes a host registry namespace: {host_namespace}"
                )


    registration_facades = (
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmAspects.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmRecipes.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmResearch.java",
    )
    for source in registration_facades:
        content = source.read_text(encoding="utf-8")
        if "HostAdapterRegistry.require()" not in content:
            failures.append(
                f"{source.relative_to(ROOT)} bypasses the host-neutral registration contract"
            )
        if "integration.tc4" in content or "thaumcraft." in content:
            failures.append(
                f"{source.relative_to(ROOT)} references a concrete host implementation"
            )
        for direct_registration in (
            "Tc4AspectAdapter.registerAspects(",
            "Tc4RecipeAdapter.registerRecipes(",
            "Tc4ResearchAdapter.register(",
        ):
            if direct_registration in content:
                failures.append(
                    f"{source.relative_to(ROOT)} registers content directly through a TC4 adapter"
                )

    bootstrap = ROOT / "src/main/java/dev/visfox/forbiddenreforged/ForbiddenMagic.java"
    bootstrap_content = bootstrap.read_text(encoding="utf-8")
    adapter_install = bootstrap_content.find("HostAdapterRegistry.install(Tc4HostAdapter.INSTANCE)")
    aspect_init = bootstrap_content.find("FmAspects.init()")
    aspect_tags = bootstrap_content.find("HostAdapterRegistry.require().registerAspectTags()")
    if adapter_install < 0 or aspect_init < 0 or adapter_install > aspect_init:
        failures.append(
            "ForbiddenMagic must install the host adapter before initializing aspect catalogs"
        )
    if aspect_tags < 0 or "Tc4AspectTagAdapter" in bootstrap_content:
        failures.append(
            "ForbiddenMagic must register aspect scan targets through HostAdapter rather than a concrete TC4 adapter"
        )


    neutral_facades = (
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmAspects.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmSetup.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmCommands.java",
    )
    for source in neutral_facades:
        source_content = source.read_text(encoding="utf-8")
        if "integration.tc4" in source_content or "thaumcraft." in source_content:
            failures.append(
                f"{source.relative_to(ROOT)} leaks a concrete host integration into a shared facade"
            )

    fm_aspects = (ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmAspects.java").read_text(encoding="utf-8")
    for host_import in ("import thaumcraft.", "import net.minecraft.", "import net.neoforged."):
        if host_import in fm_aspects:
            failures.append(
                f"FmAspects must expose ContentId values, not host/game API types ({host_import})"
            )

    host_api = (ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/api/HostAdapter.java").read_text(encoding="utf-8")
    if "crystalwellUsesShardTag()" not in host_api:
        failures.append("HostAdapter is missing the Crystalwell ingredient configuration query")
    if "void registerAspectTags()" not in host_api:
        failures.append("HostAdapter is missing the host aspect scan-target registration hook")
    for contract_method in ("initializeRuntime()", "aspectDiagnostics()"):
        if contract_method not in host_api:
            failures.append(
                f"HostAdapter is missing the host-neutral lifecycle/diagnostic contract: {contract_method}"
            )


    runtime_registry = (ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/api/HostAdapterRegistry.java").read_text(encoding="utf-8")
    if "HostRuntimeServices" in runtime_registry or "requireRuntime()" in runtime_registry:
        failures.append("HostAdapterRegistry must expose narrow runtime capabilities instead of a monolithic runtime accessor")
    if "HostEntityAspectServices requireEntityAspects()" not in runtime_registry:
        failures.append("HostAdapterRegistry is missing the entity aspect catalog lookup")
    if "HostEntityClassificationServices requireEntityClassification()" not in runtime_registry:
        failures.append("HostAdapterRegistry is missing the entity classification lookup")

    service_contracts = {
        "HostAspectServices.java": ("Component aspectDescription(ContentId aspect)",),
        "HostMorphToolServices.java": ("playMorphCycleSound(", "boolean isAreaHarvesting()", "void breakArea("),
        "HostBlockEntityServices.java": ("WrathCageBlockEntity createWrathCageBlockEntity(",),
        "HostWandInspectionServices.java": ("boolean isWand(", "@Nullable String wandRodTag(", "int wandFocusTreasure(", "int wandFocusPotency("),
        "HostWandVisServices.java": ("void addWandVis(", "void addRealVis("),
        "HostAccessoryServices.java": ("ItemStack accessory(Player player, int slot)",),
        "HostTaintBlockServices.java": ("boolean isTaintMaterial(", "boolean isTaintShovelPurificationTarget(", "float taintShovelMiningSpeed()", "void playTaintShovelSound("),
        "HostEnchantmentEffectServices.java": ("ItemStack specialMiningDrop(", "boolean isTaintDamage("),
        "HostScribingServices.java": ("boolean isNoResearchDifficulty()", "List<ContentId> primalAspectIds()", "boolean addAspectPool(ServerPlayer player",),
        "HostEntityAspectServices.java": ("Map<ContentId, ContentId> aspectCatalog(boolean silverfishEmeralds)",),
        "HostEntityClassificationServices.java": ("boolean isTaintedMob(LivingEntity entity)", "boolean isHostBoss(LivingEntity entity)", "boolean isHostGolem(LivingEntity entity)", "Set<ContentId> bossEntityIds()"),
    }
    for filename, methods in service_contracts.items():
        contract = ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/runtime" / filename
        if not contract.is_file():
            failures.append(f"Missing focused host capability contract: {filename}")
            continue
        content = contract.read_text(encoding="utf-8")
        for forbidden in ("thaumcraft.", "thaumcraft:", "com.leclowndu93150.thaumaturge.", "com.leclowndu93150.thaumaturge:"):
            if forbidden in content:
                failures.append(f"{contract.relative_to(ROOT)} exposes a concrete host API type or namespace: {forbidden}")
        for method in methods:
            if method not in content:
                failures.append(f"{filename} is missing the host-neutral capability: {method}")

    if (ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/runtime/HostRuntimeServices.java").exists():
        failures.append("The monolithic HostRuntimeServices contract should be removed after capability splitting")

    capability_lookups = (
        "HostWandInspectionServices requireWandInspection()",
        "HostWandVisServices requireWandVis()",
        "HostAccessoryServices requireAccessories()",
        "HostEntityAspectServices requireEntityAspects()",
        "HostEntityClassificationServices requireEntityClassification()",
        "HostAspectServices requireAspects()",
        "HostMorphToolServices requireMorphTools()",
        "HostBlockEntityServices requireBlockEntities()",
        "HostWandInspectionServices requireWandInspection()",
        "HostWandVisServices requireWandVis()",
        "HostAccessoryServices requireAccessories()",
        "HostTaintBlockServices requireTaintBlocks()",
        "HostEnchantmentEffectServices requireEnchantmentEffects()",
        "HostScribingServices requireScribing()",
    )
    for lookup in capability_lookups:
        if lookup not in runtime_registry:
            failures.append(f"HostAdapterRegistry is missing a focused runtime capability lookup: {lookup}")

    capability_consumers = {
        "FmSpawnerMobs.java": ("requireEntityAspects()", "requireEntityClassification()"),
        "FmDrops.java": ("requireEntityClassification()", "requireWandInspection()"),
        "RidingCropItem.java": ("requireEntityClassification()",),
        "FmEnchantmentEffects.java": ("requireEnchantmentEffects()", "requireWandInspection()", "requireWandVis()"),
        "FmToolEvents.java": ("requireWandInspection()", "requireWandVis()", "requireAccessories()", "requireTaintBlocks()"),
        "ScribeWellEvents.java": ("requireScribing()", "requireItemFactories()"),
        "MobCrystalItem.java": ("requireAspects()",),
        "MorphToolItem.java": ("requireMorphTools()",),
        "TaintShovelItem.java": ("requireTaintBlocks()",),
        "FmBlockEntities.java": ("requireBlockEntities()",),
        "WrathCageBlock.java": ("requireBlockEntities()",),
    }
    for filename, lookups in capability_consumers.items():
        source = ROOT / "src/main/java/dev/visfox/forbiddenreforged" / filename
        content = source.read_text(encoding="utf-8")
        if "requireRuntime()" in content:
            failures.append(f"{source.relative_to(ROOT)} uses the removed monolithic runtime service accessor")
        for lookup in lookups:
            if lookup not in content:
                failures.append(f"{source.relative_to(ROOT)} does not use its focused capability: {lookup}")
        if "integration.tc4" in content or "thaumcraft." in content:
            failures.append(f"{source.relative_to(ROOT)} calls a concrete host adapter instead of a host capability")

    for source_root in (ROOT / "src/main/java", ROOT / "tests/java"):
        if not source_root.is_dir():
            continue
        for source in source_root.rglob("*.java"):
            content = source.read_text(encoding="utf-8")
            if "HostRuntimeServices" in content or "requireRuntime()" in content:
                failures.append(
                    f"{source.relative_to(ROOT)} still references the removed monolithic runtime contract"
                )
            if "HostEntityServices" in content or "requireEntities()" in content:
                failures.append(
                    f"{source.relative_to(ROOT)} still references the removed mixed entity capability"
                )
            if "HostItemServices" in content or "requireItems()" in content:
                failures.append(
                    f"{source.relative_to(ROOT)} still references the removed combined item capability"
                )
            if "HostWandServices" in content or "requireWands()" in content:
                failures.append(
                    f"{source.relative_to(ROOT)} still references the removed combined wand capability"
                )
            if "HostTaintServices" in content or "requireTaint()" in content:
                failures.append(
                    f"{source.relative_to(ROOT)} still references the removed combined taint capability"
                )

    old_item_contract = ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/runtime/HostItemServices.java"
    if old_item_contract.exists():
        failures.append("The combined HostItemServices contract must be removed after the item capability split")

    old_wand_contract = ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/runtime/HostWandServices.java"
    if old_wand_contract.exists():
        failures.append("The combined HostWandServices contract must be removed after the wand capability split")

    old_taint_contract = ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/runtime/HostTaintServices.java"
    if old_taint_contract.exists():
        failures.append("The combined HostTaintServices contract must be removed after the taint capability split")

    item_property_contract = ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/runtime/HostItemPropertyServices.java"
    item_factory_contract = ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/runtime/HostItemFactoryServices.java"
    item_contracts = {
        item_property_contract: (
            "Item.Properties gluttonyFoodProperties(",
            "Item.Properties skullAxeProperties(",
            "Item.Properties morphPickaxeProperties(",
            "Item.Properties morphSwordProperties(",
            "Item.Properties morphShovelProperties(",
            "Item.Properties morphAxeProperties(",
            "Item.Properties diabolistForkProperties(",
            "Item.Properties taintShovelProperties(",
            "Item.Properties taintPickaxeProperties(",
            "Item.Properties blinkFocusProperties(",
        ),
        item_factory_contract: (
            "Item createMorphTool(",
            "Item createRingNutrition(",
            "Item createCrystalwell(",
            "Item createPrimewell(",
            "Item createBlinkFocus(",
            "Item primordialPearlItem()",
            "Item createSubCollar(",
        ),
    }
    for contract, methods in item_contracts.items():
        content = contract.read_text(encoding="utf-8")
        for forbidden in ("thaumcraft.", "com.leclowndu93150.thaumaturge."):
            if forbidden in content:
                failures.append(f"{contract.relative_to(ROOT)} exposes a concrete host API type: {forbidden}")
        for method in methods:
            if method not in content:
                failures.append(f"{contract.relative_to(ROOT)} is missing the host-neutral item capability: {method}")

    item_registry = (ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/api/HostAdapterRegistry.java").read_text(encoding="utf-8")
    if "HostItemPropertyServices requireItemProperties()" not in item_registry:
        failures.append("HostAdapterRegistry is missing the host item-property capability lookup")
    if "HostItemFactoryServices requireItemFactories()" not in item_registry:
        failures.append("HostAdapterRegistry is missing the host item-factory capability lookup")
    profile_content = (ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/api/HostProfile.java").read_text(encoding="utf-8")
    for feature in ("ITEM_PROPERTIES", "ITEM_FACTORIES"):
        if feature not in profile_content:
            failures.append(f"HostProfile is missing the item capability feature declaration: {feature}")
    for feature in ("WAND_INSPECTION", "WAND_VIS_STORAGE", "ACCESSORY_INTEGRATION"):
        if feature not in profile_content:
            failures.append(f"HostProfile is missing the wand capability feature declaration: {feature}")
    for feature in ("TAINT_BLOCK_SERVICES", "ENCHANTMENT_EFFECTS"):
        if feature not in profile_content:
            failures.append(f"HostProfile is missing the taint/enchantment capability feature declaration: {feature}")

    items_facade = ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmItems.java"
    items_content = items_facade.read_text(encoding="utf-8")
    if "integration.tc4" in items_content or "Tc4" in items_content or "thaumcraft." in items_content:
        failures.append(f"{items_facade.relative_to(ROOT)} references concrete host item classes")
    for lookup in ("requireItemProperties()", "requireItemFactories()"):
        if lookup not in items_content:
            failures.append(f"FmItems bypasses the focused item capability: {lookup}")
    if "requireItems()" in items_content:
        failures.append("FmItems still uses the removed combined item capability")

    old_entity_contract = ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/runtime/HostEntityServices.java"
    if old_entity_contract.exists():
        failures.append("The mixed HostEntityServices contract must be removed after the entity capability split")

    entity_aspect_contract = ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/runtime/HostEntityAspectServices.java"
    entity_classification_contract = ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/runtime/HostEntityClassificationServices.java"
    for contract in (entity_aspect_contract, entity_classification_contract):
        content = contract.read_text(encoding="utf-8")
        for forbidden in ("thaumcraft.", "thaumcraft:", "com.leclowndu93150.thaumaturge."):
            if forbidden in content:
                failures.append(
                    f"{contract.relative_to(ROOT)} exposes a concrete host type or namespace: {forbidden}"
                )

    profile_content = (ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/api/HostProfile.java").read_text(encoding="utf-8")
    if "ENTITY_ASPECTS" not in profile_content:
        failures.append("HostProfile is missing the entity aspect catalog feature declaration")
    if "ENTITY_CLASSIFICATION" not in profile_content:
        failures.append("HostProfile is missing the entity classification feature declaration")

    entity_consumers = {
        "FmSpawnerMobs.java": ("requireEntityAspects()", "requireEntityClassification()"),
        "FmDrops.java": ("requireEntityClassification()",),
        "RidingCropItem.java": ("requireEntityClassification()",),
    }
    for filename, lookups in entity_consumers.items():
        source = ROOT / "src/main/java/dev/visfox/forbiddenreforged" / filename
        source_content = source.read_text(encoding="utf-8")
        if "requireEntities()" in source_content:
            failures.append(f"{source.relative_to(ROOT)} still uses the removed mixed entity capability")
        for lookup in lookups:
            if lookup not in source_content:
                failures.append(f"{source.relative_to(ROOT)} does not use the focused entity capability: {lookup}")
        if "integration.tc4" in source_content or "thaumcraft." in source_content or "thaumcraft:" in source_content:
            failures.append(f"{source.relative_to(ROOT)} references concrete host entity data outside the adapter")

    spawner_content = (ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmSpawnerMobs.java").read_text(encoding="utf-8")
    if "HostAdapterRegistry.requireEntityAspects()" not in spawner_content:
        failures.append("FmSpawnerMobs bypasses the host-neutral entity aspect catalog")
    bootstrap_contract = ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/runtime/HostBootstrapServices.java"
    bootstrap_content = bootstrap_contract.read_text(encoding="utf-8")
    for forbidden in ("thaumcraft.", "com.leclowndu93150.thaumaturge."):
        if forbidden in bootstrap_content:
            failures.append(
                f"{bootstrap_contract.relative_to(ROOT)} exposes a concrete host API type: {forbidden}"
            )

    bootstrap_registry = (ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/api/HostAdapterRegistry.java").read_text(encoding="utf-8")
    if "HostBootstrapServices requireBootstrap()" not in bootstrap_registry:
        failures.append("HostAdapterRegistry is missing the host bootstrap capability lookup")

    bootstrap_consumers = (
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmWands.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmWandTriggers.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmTinkerer.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmCreativeTab.java",
    )
    for source in bootstrap_consumers:
        source_content = source.read_text(encoding="utf-8")
        if "integration.tc4" in source_content or "Tc4" in source_content or "thaumcraft." in source_content:
            failures.append(
                f"{source.relative_to(ROOT)} references a concrete host adapter instead of HostBootstrapServices"
            )
        if "HostAdapterRegistry.requireBootstrap()" not in source_content:
            failures.append(
                f"{source.relative_to(ROOT)} bypasses the host bootstrap capability contract"
            )

    production_root = ROOT / "src/main/java/dev/visfox/forbiddenreforged"
    composition_root = production_root / "ForbiddenMagic.java"
    allowed_adapter_import = "import dev.visfox.forbiddenreforged.integration.tc4.Tc4HostAdapter;"
    allowed_adapter_selection = "HostAdapterRegistry.install(Tc4HostAdapter.INSTANCE);"
    composition_content = composition_root.read_text(encoding="utf-8")
    if composition_content.count(allowed_adapter_import) != 1:
        failures.append("ForbiddenMagic must import only the selected Tc4HostAdapter as its concrete host dependency")
    if composition_content.count(allowed_adapter_selection) != 1:
        failures.append("ForbiddenMagic must install exactly one concrete host adapter")

    forbidden_host_markers = (
        "import thaumcraft.",
        "import com.leclowndu93150.thaumaturge.",
        "integration.tc4",
        "thaumcraft:",
        "com.leclowndu93150.thaumaturge:",
    )
    for source in production_root.rglob("*.java"):
        if "/integration/tc4/" in source.as_posix():
            continue
        source_content = source.read_text(encoding="utf-8")
        if source == composition_root:
            source_content = source_content.replace(allowed_adapter_import, "")
            source_content = source_content.replace(allowed_adapter_selection, "")
        for marker in forbidden_host_markers:
            if marker in source_content:
                failures.append(
                    f"{source.relative_to(ROOT)} leaks a concrete host dependency outside integration/tc4: {marker}"
                )
        if re.search(r"\b(?:TaintedMob|ThaumcraftBoss|TcBlockEntity|AspectSource|EssentiaTransport|Wands)\b", source_content):
            failures.append(
                f"{source.relative_to(ROOT)} references a concrete TC4 runtime type outside integration/tc4"
            )
        if re.search(r"\bTc4[A-Z][A-Za-z0-9_]*\b", source_content):
            failures.append(
                f"{source.relative_to(ROOT)} references a concrete TC4 adapter outside the composition root"
            )

    if failures:
        print("\n".join(failures), file=sys.stderr)
        return 1

    print(f"Host-agnostic API boundary check passed ({checked} Java files).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
