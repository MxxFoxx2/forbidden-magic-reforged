import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
LANG_SOURCE = ROOT / "data" / "lang"
ASSETS = ROOT / "src" / "generated" / "resources" / "assets" / "forbiddenmagic"


def flatten(value: object, prefix: str = "") -> dict[str, str]:
    if isinstance(value, dict):
        result: dict[str, str] = {}
        for key, nested in value.items():
            result.update(flatten(nested, prefix + str(key) + "."))
        return result
    return {prefix.rstrip("."): str(value)}


def write_json(path: Path, data: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2, sort_keys=True) + "\n", encoding="utf-8", newline="\n")
