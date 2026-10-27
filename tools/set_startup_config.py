import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CONFIG = ROOT / "run" / "config" / "forbiddenmagic-startup.toml"


def main() -> int:
    if len(sys.argv) != 2 or "=" not in sys.argv[1]:
        raise SystemExit("usage: set_startup_config.py key=value (e.g. no_lust=true)")
    key, value = (part.strip() for part in sys.argv[1].split("=", 1))
    if not re.fullmatch(r"[A-Za-z_][A-Za-z0-9_]*", key):
        raise SystemExit(f"not a config key: {key!r}")
    if not CONFIG.is_file():
        raise SystemExit(f"Expected startup config was not generated: {CONFIG}")
    original = CONFIG.read_text(encoding="utf-8")
    pattern = re.compile(rf"(?m)^([ \t]*{re.escape(key)}[ \t]*=[ \t]*)[^#\n]*?([ \t]*(?:#.*)?[ \t]*$)")
    updated, count = pattern.subn(lambda m: m.group(1) + value + m.group(2), original)
    if count != 1:
        raise SystemExit(f"Expected exactly one '{key} =' setting in {CONFIG}, found {count}")
    CONFIG.write_text(updated, encoding="utf-8")
    print(f"Set {key}={value} in {CONFIG}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
