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
    if adapter_install < 0 or aspect_init < 0 or adapter_install > aspect_init:
        failures.append(
            "ForbiddenMagic must install the host adapter before initializing aspect catalogs"
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
    for contract_method in ("initializeRuntime()", "aspectDiagnostics()"):
        if contract_method not in host_api:
            failures.append(
                f"HostAdapter is missing the host-neutral lifecycle/diagnostic contract: {contract_method}"
            )

    if failures:
        print("\n".join(failures), file=sys.stderr)
        return 1

    print(f"Host-agnostic API boundary check passed ({checked} Java files).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
