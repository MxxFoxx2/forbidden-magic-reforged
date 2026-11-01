from pathlib import Path
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
            if "Wands." in content or "import thaumcraft.wand.Wands;" in content:
                failures.append(
                    f"{source.relative_to(ROOT)} accesses the TC4 wand API outside integration/tc4"
                )

    if failures:
        print("\\n".join(failures), file=sys.stderr)
        return 1

    print(f"Host-agnostic API boundary check passed ({checked} Java files).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
