from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
SOURCE_ROOT = ROOT / "src/main/java/dev/visfox/forbiddenreforged"
CORE_ROOT = SOURCE_ROOT / "core"
API_ROOT = SOURCE_ROOT / "integration/api"
RUNTIME_ROOT = SOURCE_ROOT / "integration/runtime"
TC4_ROOT = SOURCE_ROOT / "integration/tc4"
COMPOSITION_ROOT = SOURCE_ROOT / "ForbiddenMagic.java"

HOST_PACKAGES = (
    "thaumcraft",
    "com.leclowndu93150.thaumaturge",
)
CORE_FORBIDDEN_PACKAGES = (
    "net.minecraft",
    "net.neoforged",
    *HOST_PACKAGES,
)
IMPORT_RE = re.compile(
    r"^\s*import\s+(?:static\s+)?([\w.]+)(?:\.\*)?\s*;",
    re.MULTILINE,
)
HOST_REFERENCE_RE = re.compile(
    r"\b(?:thaumcraft|com\.leclowndu93150\.thaumaturge)(?:\.|:)"
)


def java_files(root: Path):
    if root.is_file() and root.suffix == ".java":
        return (root,)
    if root.is_dir():
        return sorted(root.rglob("*.java"))
    return ()


def imported_names(source: str):
    return IMPORT_RE.findall(source)


def belongs_to(path: Path, directory: Path) -> bool:
    return path == directory or directory in path.parents


def has_package_prefix(name: str, prefix: str) -> bool:
    return name == prefix or name.startswith(prefix + ".")


def main() -> int:
    failures = []
    checked = 0

    required_roots = (SOURCE_ROOT, CORE_ROOT, API_ROOT, RUNTIME_ROOT, TC4_ROOT)
    for root in required_roots:
        if not root.exists():
            failures.append(f"Missing source boundary: {root.relative_to(ROOT)}")

    # Shared models and behaviors must not depend on Minecraft, NeoForge or a host.
    for source in java_files(CORE_ROOT):
        checked += 1
        content = source.read_text(encoding="utf-8")
        for imported in imported_names(content):
            if any(has_package_prefix(imported, package) for package in CORE_FORBIDDEN_PACKAGES):
                failures.append(
                    f"{source.relative_to(ROOT)} imports forbidden core dependency: {imported}"
                )
            if has_package_prefix(imported, "dev.visfox.forbiddenreforged") and not has_package_prefix(
                imported, "dev.visfox.forbiddenreforged.core"
            ):
                failures.append(
                    f"{source.relative_to(ROOT)} depends on mod composition code: {imported}"
                )
        if HOST_REFERENCE_RE.search(content):
            failures.append(
                f"{source.relative_to(ROOT)} contains a concrete host namespace or package"
            )

    # Public integration contracts may use game types, but must not expose host APIs.
    for boundary in (API_ROOT, RUNTIME_ROOT):
        for source in java_files(boundary):
            checked += 1
            content = source.read_text(encoding="utf-8")
            for imported in imported_names(content):
                if any(has_package_prefix(imported, package) for package in HOST_PACKAGES):
                    failures.append(
                        f"{source.relative_to(ROOT)} exposes a concrete host import: {imported}"
                    )
            if HOST_REFERENCE_RE.search(content):
                failures.append(
                    f"{source.relative_to(ROOT)} contains a concrete host namespace or package"
                )

    # Production host references belong in integration/tc4. The composition root
    # is the single intentional place that selects the active adapter.
    checked_boundaries = (CORE_ROOT, API_ROOT, RUNTIME_ROOT, TC4_ROOT)
    for source in java_files(SOURCE_ROOT):
        if any(belongs_to(source, boundary) for boundary in checked_boundaries):
            continue

        checked += 1
        content = source.read_text(encoding="utf-8")
        for imported in imported_names(content):
            if any(has_package_prefix(imported, package) for package in HOST_PACKAGES):
                failures.append(
                    f"{source.relative_to(ROOT)} imports host implementation outside integration/tc4: {imported}"
                )
            if has_package_prefix(imported, "dev.visfox.forbiddenreforged.integration.tc4"):
                if source != COMPOSITION_ROOT:
                    failures.append(
                        f"{source.relative_to(ROOT)} selects a concrete TC4 adapter outside the composition root"
                    )

        if HOST_REFERENCE_RE.search(content):
            failures.append(
                f"{source.relative_to(ROOT)} references a concrete host outside integration/tc4"
            )

    if failures:
        print("\n".join(failures), file=sys.stderr)
        return 1

    print(f"Host API boundaries passed ({checked} Java files checked).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
