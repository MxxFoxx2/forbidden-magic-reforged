from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
NEUTRAL_SCOPES = (
    ROOT / "src/main/java/dev/visfox/forbiddenreforged/core",
    ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/api",
    ROOT / "src/main/java/dev/visfox/forbiddenreforged/integration/runtime",
)
FORBIDDEN = (
    (re.compile(r"\\bimport\\s+(?:static\\s+)?thaumcraft\\."), "Thaumcraft import"),
    (re.compile(r"\\bthaumcraft\\.[A-Za-z_]"), "fully-qualified Thaumcraft reference"),
    (re.compile(r"\\bimport\\s+(?:static\\s+)?net\\.neoforged\\."), "NeoForge import"),
    (re.compile(r"\\bnet\\.neoforged\\.[A-Za-z_]"), "fully-qualified NeoForge reference"),
    (re.compile(r"dev\\.visfox\\.forbiddenreforged\\.integration\\.tc4"), "concrete TC4 adapter reference"),
    (re.compile(r"\\bTc4[A-Z][A-Za-z0-9_]*\\b"), "concrete TC4 adapter type"),
)


def main() -> int:
    failures: list[str] = []
    scanned = 0

    for scope in NEUTRAL_SCOPES:
        if not scope.is_dir():
            failures.append(f"missing architecture scope: {scope.relative_to(ROOT)}")
            continue

        for source in sorted(scope.rglob("*.java")):
            scanned += 1
            try:
                lines = source.read_text(encoding="utf-8").splitlines()
            except OSError as error:
                failures.append(f"{source.relative_to(ROOT)}: cannot read source: {error}")
                continue

            for line_number, line in enumerate(lines, start=1):
                for pattern, description in FORBIDDEN:
                    if pattern.search(line):
                        failures.append(
                            f"{source.relative_to(ROOT)}:{line_number}: "
                            f"{description}: {line.strip()}"
                        )

    if failures:
        print("Host-agnostic architecture boundary check failed:")
        print("\n".join(failures))
        return 1

    print(f"Host-agnostic architecture boundary check passed ({scanned} Java files).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
