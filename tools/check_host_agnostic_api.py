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
    ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/runtime/HostEntityServices.java",
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


    runtime_contract = ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/runtime/HostRuntimeServices.java"
    runtime_content = runtime_contract.read_text(encoding="utf-8")
    for forbidden in ("thaumcraft.", "com.leclowndu93150.thaumaturge."):
        if forbidden in runtime_content:
            failures.append(
                f"{runtime_contract.relative_to(ROOT)} exposes a concrete host API type: {forbidden}"
            )

    runtime_consumers = (
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmDrops.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmEnchantmentEffects.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmToolEvents.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/RidingCropItem.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmBlockEntities.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/WrathCageBlock.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/ScribeWellEvents.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/MobCrystalItem.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/MorphToolItem.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/TaintShovelItem.java",
    )
    for source in runtime_consumers:
        source_content = source.read_text(encoding="utf-8")
        if "integration.tc4" in source_content or "thaumcraft." in source_content:
            failures.append(
                f"{source.relative_to(ROOT)} calls a concrete host adapter instead of HostRuntimeServices"
            )

    runtime_registry = (ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/api/HostAdapterRegistry.java").read_text(encoding="utf-8")
    if "HostRuntimeServices requireRuntime()" not in runtime_registry:
        failures.append("HostAdapterRegistry is missing the runtime capability lookup")
    if "HostEntityServices requireEntities()" not in runtime_registry:
        failures.append("HostAdapterRegistry is missing the host entity capability lookup")
    for contract_method in (
        "Component aspectDescription(ContentId aspect)",
        "void playMorphCycleSound(",
        "boolean isAreaHarvesting()",
        "void breakArea(",
        "boolean isTaintShovelPurificationTarget(",
        "float taintShovelMiningSpeed()",
        "void playTaintShovelSound(",
    ):
        if contract_method not in runtime_content:
            failures.append(f"HostRuntimeServices is missing a shared game-runtime capability: {contract_method}")
    if "HostItemServices requireItems()" not in runtime_registry:
        failures.append("HostAdapterRegistry is missing the host item factory lookup")
    if "List<ContentId> primalAspectIds()" not in runtime_content or "boolean addAspectPool(ServerPlayer player" not in runtime_content:
        failures.append("HostRuntimeServices is missing the host-neutral Scribing Well aspect-pool contract")
    if "WrathCageBlockEntity createWrathCageBlockEntity(" not in runtime_content:
        failures.append("HostRuntimeServices is missing the host-provided Wrath Cage block entity factory")


    item_contract = ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/runtime/HostItemServices.java"
    item_content = item_contract.read_text(encoding="utf-8")
    for forbidden in ("thaumcraft.", "com.leclowndu93150.thaumaturge."):
        if forbidden in item_content:
            failures.append(
                f"{item_contract.relative_to(ROOT)} exposes a concrete host API type: {forbidden}"
            )

    item_registry = (ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/api/HostAdapterRegistry.java").read_text(encoding="utf-8")
    if "HostItemServices requireItems()" not in item_registry:
        failures.append("HostAdapterRegistry is missing the host item factory lookup")

    entity_contract = ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/runtime/HostEntityServices.java"
    entity_content = entity_contract.read_text(encoding="utf-8")
    for forbidden in ("thaumcraft.", "thaumcraft:", "com.leclowndu93150.thaumaturge."):
        if forbidden in entity_content:
            failures.append(
                f"{entity_contract.relative_to(ROOT)} exposes a concrete host type or namespace: {forbidden}"
            )
    if "HostEntityServices requireEntities()" not in runtime_registry:
        failures.append("HostAdapterRegistry is missing the host entity capability lookup")
    if "Map<String, ContentId> aspectCatalog(boolean silverfishEmeralds)" not in entity_content:
        failures.append("HostEntityServices is missing the host-neutral entity aspect catalog")
    entity_consumers = (
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmSpawnerMobs.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmDrops.java",
        ROOT / "src/main/java/dev/visfox/forbiddenreforged/RidingCropItem.java",
    )
    for source in entity_consumers:
        source_content = source.read_text(encoding="utf-8")
        if "integration.tc4" in source_content or "thaumcraft." in source_content or "thaumcraft:" in source_content:
            failures.append(
                f"{source.relative_to(ROOT)} references concrete host entity data instead of HostEntityServices"
            )
    if "HostAdapterRegistry.requireEntities()" not in (ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmSpawnerMobs.java").read_text(encoding="utf-8"):
        failures.append("FmSpawnerMobs bypasses the host-neutral entity catalog")

    if "Item primordialPearlItem()" not in item_content:
        failures.append("HostItemServices is missing the Primewell remainder item factory")

    items_facade = ROOT / "src/main/java/dev/visfox/forbiddenreforged/FmItems.java"
    items_content = items_facade.read_text(encoding="utf-8")
    if "integration.tc4" in items_content or "Tc4" in items_content or "thaumcraft." in items_content:
        failures.append(
            f"{items_facade.relative_to(ROOT)} references concrete host item classes instead of HostItemServices"
        )
    if "HostAdapterRegistry.requireItems()" not in items_content:
        failures.append("FmItems bypasses the host-neutral item factory contract")


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

    if failures:
        print("\n".join(failures), file=sys.stderr)
        return 1

    print(f"Host-agnostic API boundary check passed ({checked} Java files).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
