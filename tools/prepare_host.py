import argparse
import os
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
HOST = ROOT / "host"
UPSTREAM = ROOT.parent / "thaumcraft-4-reforged"
PATCHES = ROOT / "patches"


def sha() -> str:
    return (ROOT / "host.sha").read_text(encoding="utf-8").strip()


def run(*args: str, cwd: Path | None = None) -> None:
    result = subprocess.run(list(args), cwd=cwd if cwd else ROOT, text=True)
    if result.returncode != 0:
        raise SystemExit(f"command failed: {' '.join(args)}")


def clone() -> None:
    if not HOST.exists():
        subprocess.run(
            ["git", "clone", "--no-hardlinks", str(UPSTREAM), str(HOST)],
            check=True,
            text=True,
        )
    run("git", "-C", str(HOST), "config", "core.autocrlf", "false")
    run("git", "-C", str(HOST), "config", "user.name", "Forbidden Magic Port Build")
    run("git", "-C", str(HOST), "config", "user.email", "port-build@example.invalid")
    run("git", "-C", str(HOST), "fetch", "--all")
    run("git", "-C", str(HOST), "checkout", "--force", "--detach", sha())
    run("git", "-C", str(HOST), "read-tree", "HEAD")
    run("git", "-C", str(HOST), "checkout-index", "-a", "-f")
    run("git", "-C", str(HOST), "clean", "-f", "-d")


def apply_patches() -> None:
    for patch in sorted(PATCHES.glob("*.patch")):
        check = subprocess.run(
            ["git", "-C", str(HOST), "apply", "--check", "--ignore-space-change", str(patch)],
            text=True,
        )
        if check.returncode != 0:
            raise SystemExit(f"patch does not apply cleanly: {patch.name}")
        run("git", "-C", str(HOST), "am", "--ignore-space-change", "--no-gpg-sign", str(patch))


def verify() -> None:
    if not HOST.exists():
        raise SystemExit("host/ missing: run without --verify first")
    expected = sha()
    ancestor = subprocess.run(
        ["git", "-C", str(HOST), "merge-base", "--is-ancestor", expected, "HEAD"],
        text=True,
    )
    if ancestor.returncode != 0:
        raise SystemExit(f"host/ does not descend from pinned sha {expected}")
    count = int(
        subprocess.run(
            ["git", "-C", str(HOST), "rev-list", "--count", f"{expected}..HEAD"],
            capture_output=True,
            text=True,
            check=True,
        ).stdout.strip()
    )
    patch_count = len(list(PATCHES.glob("*.patch")))
    if count != patch_count:
        raise SystemExit(f"host/ is {count} commits ahead, patches/ holds {patch_count}")
    dirty = subprocess.run(
        ["git", "-C", str(HOST), "status", "--porcelain", "--untracked-files=no"],
        capture_output=True,
        text=True,
    ).stdout
    if dirty.strip():
        raise SystemExit("host/ has unpatched edits, they must live in patches/ instead:\n" + dirty)
    print(f"host matches sha + {patch_count} patch commit(s)")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--verify", action="store_true")
    parser.add_argument("--rebuild", action="store_true")
    arguments = parser.parse_args()
    if arguments.verify:
        verify()
        return 0
    clone()
    apply_patches()
    if arguments.rebuild:
        if os.name == "nt":
            run("cmd", "//c", "gradlew.bat", "build", cwd=HOST)
        else:
            run("./gradlew", "build", cwd=HOST)
    return 0


if __name__ == "__main__":
    sys.exit(main())
