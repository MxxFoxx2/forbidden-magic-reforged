from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE_ROOT = ROOT / "src/main/java/dev/visfox/forbiddenreforged"
TC4_SCOPE = SOURCE_ROOT / "integration/tc4"
COMPOSITION_ROOT = SOURCE_ROOT / "ForbiddenMagic.java"
NEUTRAL_SCOPES = (
    SOURCE_ROOT / "core",
    SOURCE_ROOT / "integration/api",
    SOURCE_ROOT / "integration/runtime",
)
FORBIDDEN_NEUTRAL = (
    (re.compile(r"\bimport\s+(?:static\s+)?thaumcraft\."), "Thaumcraft import"),
    (re.compile(r"\bthaumcraft\.[A-Za-z_]"), "fully-qualified Thaumcraft reference"),
    (re.compile(r"\bimport\s+(?:static\s+)?net\.neoforged\."), "NeoForge import"),
    (re.compile(r"\bnet\.neoforged\.[A-Za-z_]"), "fully-qualified NeoForge reference"),
    (re.compile(r"dev\.visfox\.forbiddenreforged\.integration\.tc4"), "concrete TC4 adapter reference"),
    (re.compile(r"\bTc4[A-Z][A-Za-z0-9_]*\b"), "concrete TC4 adapter type"),
)
FORBIDDEN_HOST = (
    (re.compile(r"\bimport\s+(?:static\s+)?thaumcraft\."), "Thaumcraft import"),
    (re.compile(r"\bthaumcraft\.[A-Za-z_]"), "fully-qualified Thaumcraft reference"),
    (re.compile(r"dev\.visfox\.forbiddenreforged\.integration\.tc4"), "concrete TC4 adapter reference"),
    (re.compile(r"\bTc4[A-Z][A-Za-z0-9_]*\b"), "concrete TC4 adapter type"),
)
COMPOSITION_ROOT_ALLOWLIST = {
    "import dev.visfox.forbiddenreforged.integration.tc4.Tc4HostAdapter;"
}


def scan_file(source: Path, patterns: tuple[tuple[re.Pattern[str], str], ...],
              failures: list[str], allowlisted_lines: set[str] | None = None) -> None:
    try:
        lines = source.read_text(encoding="utf-8").splitlines()
    except OSError as error:
        failures.append(f"{source.relative_to(ROOT)}: cannot read source: {error}")
        return

    for line_number, line in enumerate(lines, start=1):
        if allowlisted_lines and line.strip() in allowlisted_lines:
            continue
        for pattern, description in patterns:
            if pattern.search(line):
                failures.append(
                    f"{source.relative_to(ROOT)}:{line_number}: "
                    f"{description}: {line.strip()}"
                )


def main() -> int:
    failures: list[str] = []
    neutral_files = 0
    production_files = 0

    for scope in NEUTRAL_SCOPES:
        if not scope.is_dir():
            failures.append(f"missing architecture scope: {scope.relative_to(ROOT)}")
            continue

        for source in sorted(scope.rglob("*.java")):
            neutral_files += 1
            scan_file(source, FORBIDDEN_NEUTRAL, failures)

    if not SOURCE_ROOT.is_dir():
        failures.append(f"missing production source root: {SOURCE_ROOT.relative_to(ROOT)}")
    else:
        for source in sorted(SOURCE_ROOT.rglob("*.java")):
            if source.is_relative_to(TC4_SCOPE):
                continue
            production_files += 1
            allowlist = COMPOSITION_ROOT_ALLOWLIST if source == COMPOSITION_ROOT else None
            scan_file(source, FORBIDDEN_HOST, failures, allowlist)

    if failures:
        print("Host-agnostic architecture boundary check failed:")
        print("\n".join(failures))
        return 1

    print(
        "Host-agnostic architecture boundary check passed "
        f"({neutral_files} neutral Java files; "
        f"{production_files} production Java files checked outside the TC4 adapter)."
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
