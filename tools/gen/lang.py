import tomllib

from .context import ASSETS, LANG_SOURCE, flatten, write_json


def generate_lang() -> None:
    key_sets: dict[str, set[str]] = {}
    for source in sorted(LANG_SOURCE.glob("*.toml")):
        data = tomllib.loads(source.read_text(encoding="utf-8"))
        entries = flatten(data)
        key_sets[source.stem] = set(entries)
        write_json(ASSETS / "lang" / (source.stem + ".json"), entries)
        print(f"lang/{source.stem}.json: {len(entries)} keys")
    if len(key_sets) > 1:
        reference_name = "en_us" if "en_us" in key_sets else next(iter(key_sets))
        reference = key_sets[reference_name]
        for name, keys in key_sets.items():
            if keys == reference:
                continue
            missing = sorted(reference - keys)
            extra = sorted(keys - reference)
            details = ", ".join(
                part for part in (
                    f"missing: {missing[:10]}" if missing else "",
                    f"extra: {extra[:10]}" if extra else "",
                ) if part
            )
            raise SystemExit(f"lang/{name}.toml diverges from {reference_name} ({details})")
