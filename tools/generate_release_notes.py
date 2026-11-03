#!/usr/bin/env python3
"""Render the notes body for a rolling GitHub release.

The workflow supplies the already-selected release metadata through environment
variables. This script only formats the notes; publishing remains in the
workflow.
"""
from __future__ import annotations

import argparse
import os
import re
import subprocess
import sys
from pathlib import Path

_PREVIOUS_SOURCE_SHA = re.compile(r"<!-- release-source-sha: ([0-9a-f]{40}) -->")


def render_release_notes(
    repo: str,
    source_sha: str,
    release_version: str,
    mc_version: str,
    repo_dir: Path,
    previous_body: str = "",
) -> str:
    """Render the release body using the same visible format as the workflow."""
    previous_match = _PREVIOUS_SOURCE_SHA.search(previous_body)
    previous_sha = previous_match.group(1) if previous_match else ""
    compare_url = (
        f"https://github.com/{repo}/compare/{previous_sha}...{source_sha}"
        if previous_sha and previous_sha != source_sha
        else ""
    )

    commit_subject = subprocess.run(
        ["git", "-C", str(repo_dir), "show", "-s", "--format=%s", source_sha],
        check=True,
        capture_output=True,
        text=True,
    ).stdout.strip()

    lines = [
        f"Version {release_version} for Minecraft {mc_version}",
        "",
        f"\x60{source_sha[:7]}\x60: {commit_subject}",
    ]
    if compare_url:
        lines += ["", f"[Changes since previous build]({compare_url})"]

    major_notes_path = repo_dir / "docs" / "releases" / f"{release_version}.md"
    if major_notes_path.is_file():
        major_notes = major_notes_path.read_text(encoding="utf-8").strip()
        if major_notes:
            lines += ["", major_notes]

    lines += [
        "",
        f"<!-- release-source-sha: {source_sha} -->",
        f"<!-- release-version: {release_version} -->",
    ]
    return "\n".join(lines) + "\n"


def write_release_notes(
    output_file: Path,
    repo: str,
    source_sha: str,
    release_version: str,
    mc_version: str,
    repo_dir: Path,
    previous_body: str = "",
) -> None:
    notes = render_release_notes(
        repo=repo,
        source_sha=source_sha,
        release_version=release_version,
        mc_version=mc_version,
        repo_dir=repo_dir,
        previous_body=previous_body,
    )
    output_file.write_text(notes, encoding="utf-8", newline="\n")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("output_file", type=Path)
    args = parser.parse_args()

    required = ("REPO", "SOURCE_SHA", "RELEASE_VERSION", "MC_VERSION", "REPO_DIR")
    missing = [name for name in required if not os.environ.get(name)]
    if missing:
        print(f"release notes error: missing environment variables: {', '.join(missing)}", file=sys.stderr)
        return 2

    try:
        write_release_notes(
            output_file=args.output_file,
            repo=os.environ["REPO"],
            source_sha=os.environ["SOURCE_SHA"],
            release_version=os.environ["RELEASE_VERSION"],
            mc_version=os.environ["MC_VERSION"],
            repo_dir=Path(os.environ["REPO_DIR"]),
            previous_body=os.environ.get("PREVIOUS_BODY", ""),
        )
    except (OSError, subprocess.CalledProcessError) as exc:
        print(f"release notes error: {exc}", file=sys.stderr)
        return 2

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
