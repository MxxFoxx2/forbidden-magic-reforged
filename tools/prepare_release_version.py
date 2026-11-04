#!/usr/bin/env python3
"""Choose the next rolling release version and update Gradle metadata.

The workflow passes the base version from release-version.txt and the previous
version parsed from the channel's rolling GitHub release. Invalid versions
fail closed instead of silently resetting the release sequence.
"""
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

_VERSION_PATTERN = re.compile(r"(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)")


def parse_version(value: str, label: str) -> tuple[int, int, int]:
    match = _VERSION_PATTERN.fullmatch(value)
    if match is None:
        raise ValueError(f"{label} must be MAJOR.MINOR.PATCH, got {value!r}")
    return tuple(int(part) for part in match.groups())


def select_release_version(base_version: str, previous_version: str = "") -> str:
    base = parse_version(base_version, "base version")
    if not previous_version:
        selected = base
    else:
        previous = parse_version(previous_version, "previous release version")
        selected = base if base > previous else (previous[0], previous[1], previous[2] + 1)
    return ".".join(str(part) for part in selected)


def write_mod_version(properties_file: Path, mod_version: str) -> None:
    content = properties_file.read_text(encoding="utf-8")
    updated, count = re.subn(
        r"(?m)^mod_version=[^\r\n]*$",
        lambda _match: f"mod_version={mod_version}",
        content,
    )
    if count != 1:
        raise ValueError(f"expected one mod_version entry in {properties_file}; found {count}")
    properties_file.write_text(updated, encoding="utf-8")


def prepare_release_version(
    base_version: str,
    previous_version: str,
    properties_file: Path,
    channel: str,
) -> str:
    if not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9._-]*", channel):
        raise ValueError(f"invalid release channel: {channel!r}")
    release_version = select_release_version(base_version, previous_version)
    write_mod_version(properties_file, f"{release_version}-{channel}")
    return release_version


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("base_version")
    parser.add_argument("previous_version")
    parser.add_argument("gradle_properties", type=Path)
    parser.add_argument("channel")
    args = parser.parse_args()

    try:
        release_version = prepare_release_version(
            args.base_version,
            args.previous_version,
            args.gradle_properties,
            args.channel,
        )
    except (OSError, ValueError) as exc:
        print(f"release version error: {exc}", file=sys.stderr)
        return 2

    print(release_version)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
