#!/usr/bin/env python3
"""Check the pinned host commit against upstream without changing host.sha.

Verifies that:
1. the pinned sha in host.sha still exists upstream;
2. every patch in patches/ still applies cleanly on top of the pin
   (checked in a temporary worktree of the local upstream clone, never in host/);
3. reports how far upstream main has drifted past the pin.

Exit code 0 = pin is intact and patches apply. Exit 1 = drift/patch conflict;
the fix is a deliberate host.sha bump in a separate change, never automatic.
"""
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
UPSTREAM = ROOT.parent / "thaumcraft-4-reforged"
UPSTREAM_URL = "https://github.com/notlikethiss/thaumcraft-4-reforged.git"
PATCHES = ROOT / "patches"


def git(*args: str, cwd: Path | None = None) -> subprocess.CompletedProcess:
    return subprocess.run(
        ["git", *args], cwd=cwd, capture_output=True, text=True, check=False
    )


def main() -> int:
    pinned = (ROOT / "host.sha").read_text(encoding="utf-8").strip()
    if not UPSTREAM.exists():
        print(f"upstream clone missing at {UPSTREAM}; cloning {UPSTREAM_URL}")
        clone = subprocess.run(["git", "clone", UPSTREAM_URL, str(UPSTREAM)], text=True)
        if clone.returncode != 0:
            print("failed to clone upstream", file=sys.stderr)
            return 1
    git("-C", str(UPSTREAM), "fetch", "--all", "--quiet")

    if git("-C", str(UPSTREAM), "cat-file", "-t", pinned).returncode != 0:
        print(f"FAIL: pinned sha {pinned} no longer exists upstream", file=sys.stderr)
        return 1
    print(f"pinned sha {pinned} exists upstream")

    patches = sorted(PATCHES.glob("*.patch"))
    if not patches:
        print("FAIL: patches/ is empty", file=sys.stderr)
        return 1

    worktree = UPSTREAM / ".pin-check-worktree"
    git("-C", str(UPSTREAM), "worktree", "remove", "--force", str(worktree))
    add = git("-C", str(UPSTREAM), "worktree", "add", "--detach", str(worktree), pinned)
    if add.returncode != 0:
        print(f"FAIL: cannot create check worktree: {add.stderr.strip()}", file=sys.stderr)
        return 1
    try:
        ok = True
        for patch in patches:
            check = git(
                "-C", str(worktree), "apply", "--check", "--ignore-space-change", str(patch)
            )
            if check.returncode != 0:
                ok = False
                print(f"CONFLICT: {patch.name} no longer applies on pin {pinned[:12]}", file=sys.stderr)
                print(check.stderr.strip(), file=sys.stderr)
            else:
                print(f"applies: {patch.name}")
        if not ok:
            return 1
    finally:
        git("-C", str(UPSTREAM), "worktree", "remove", "--force", str(worktree))

    main_ref = "origin/main"
    if git("-C", str(UPSTREAM), "rev-parse", "--verify", main_ref).returncode != 0:
        main_ref = "origin/HEAD"
    drift = git("-C", str(UPSTREAM), "rev-list", "--count", f"{pinned}..{main_ref}")
    ahead = int(drift.stdout.strip() or 0)
    head = git("-C", str(UPSTREAM), "rev-parse", "--short", main_ref).stdout.strip()
    print(f"upstream {main_ref} is {ahead} commit(s) ahead of the pin (now at {head})")
    if ahead:
        print("note: host.sha is intentionally NOT updated here; bump it in a dedicated change")
    print("host pin check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
