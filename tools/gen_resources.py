import json
import tomllib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LANG_SOURCE = ROOT / "data" / "lang"
ASSETS = ROOT / "src" / "generated" / "resources" / "assets" / "forbiddenmagic"
TEXTURES = ROOT / "src" / "main" / "resources" / "assets" / "forbiddenmagic" / "textures" / "item"

ITEM_TEXTURES = {
    "wrath_shard": "wrathshard",
    "envy_shard": "envyshard",
    "taint_shard": "taintshard",
    "pride_shard": "prideshard",
    "lust_shard": "lustshard",
    "sloth_shard": "slothshard",
    "greed_shard": "greedshard",
    "gluttony_shard": "gluttonyshard",
    "skull_axe": "skullaxe",
    "emerald_fragment": "emerald_nugget",
    "tainted_blood_shard": "tainted_blood_shard",
    "diabolist_fork": "diabolist_fork",
    "taint_coal": "taint_coal",
    "riding_crop": "riding_crop",
    "black_dye": "black_dye",
    "taint_fruit": "taint_fruit",
    "ring_nutrition": "ring_nutrition",
    "alchemical_cap": "wand_cap_alchemical",
    "vinteum_cap": "wand_cap_vinteum",
    "terrasteel_cap": "wand_cap_terrasteel",
    "manasteel_cap": "wand_cap_manasteel",
    "manasteel_inert_cap": "wand_cap_manasteel_inert",
    "elementium_cap": "wand_cap_elementium",
    "elementium_inert_cap": "wand_cap_elementium_inert",
    "equivalent_rod": "wand_rod_equivalent",
    "tainted_rod": "tainted_rod",
    "infernal_rod": "infernal_rod",
    "witchwood_rod": "witchwood_rod",
    "profane_rod": "profane_rod",
    "witchwood_staff_rod": "witchwood_staff_rod",
    "taint_shovel": "taint_shovel",
    "taint_pickaxe": "taint_pickaxe",
    "crystalwell": "crystalwell",
    "primewell": "primewell",
    "sub_collar": "collar",
}

MORPH_TEXTURES = {
    "morph_pickaxe": ("chameleonpick", "eyepick"),
    "morph_sword": ("chameleonsword", "eyesword"),
    "morph_shovel": ("chameleonshovel", "eyeshovel"),
    "morph_axe": ("chameleonaxe", "eyeaxe"),
}

# Extra layer0 models referenced from conditional item definitions (model name -> texture name).
EXTRA_ITEM_MODELS = {
    "diabolist_fork_spork": "spork",
    "lust_shard_despair": "despair_shard",
}

# Items whose definition picks a model with a condition (item -> (property, on_true model)).
ITEM_CONDITIONS = {
    "diabolist_fork": ("forbiddenmagic:spork", "diabolist_fork_spork"),
    "lust_shard": ("forbiddenmagic:no_lust", "lust_shard_despair"),
}

# Two-layer items: base texture + ornament overlay (id == layer0 texture name).
TWO_LAYER_TEXTURES = {
    "blink_focus": "blink_focus_ornament",
}


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


def generate_items() -> None:
    missing = [name for name in ITEM_TEXTURES if not (TEXTURES / f"{name}.png").is_file()]
    missing += [texture for texture in EXTRA_ITEM_MODELS.values() if not (TEXTURES / f"{texture}.png").is_file()]
    if missing:
        raise SystemExit(f"no texture for {missing}")
    for name in sorted(ITEM_TEXTURES):
        write_json(ASSETS / "models" / "item" / f"{name}.json", {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"forbiddenmagic:item/{name}"},
        })
        condition = ITEM_CONDITIONS.get(name)
        if condition is None:
            definition = {"model": {"type": "minecraft:model", "model": f"forbiddenmagic:item/{name}"}}
        else:
            property_id, on_true = condition
            definition = {"model": {
                "type": "minecraft:condition",
                "property": property_id,
                "on_true": {"type": "minecraft:model", "model": f"forbiddenmagic:item/{on_true}"},
                "on_false": {"type": "minecraft:model", "model": f"forbiddenmagic:item/{name}"},
            }}
        write_json(ASSETS / "items" / f"{name}.json", definition)
    for name, texture in sorted(EXTRA_ITEM_MODELS.items()):
        write_json(ASSETS / "models" / "item" / f"{name}.json", {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"forbiddenmagic:item/{texture}"},
        })
    print(f"items: {len(ITEM_TEXTURES)} models and definitions, {len(EXTRA_ITEM_MODELS)} extra models")


def generate_morph_items() -> None:
    for name, (base, overlay) in sorted(MORPH_TEXTURES.items()):
        write_json(ASSETS / "models" / "item" / f"{name}.json", {
            "parent": "minecraft:item/generated",
            "textures": {
                "layer0": f"forbiddenmagic:item/{base}",
                "layer1": f"forbiddenmagic:item/{overlay}",
            },
        })
        write_json(ASSETS / "items" / f"{name}.json", {
            "model": {
                "type": "minecraft:model",
                "model": f"forbiddenmagic:item/{name}",
                "tints": [
                    {"type": "minecraft:constant", "value": 16777215},
                    {"type": "forbiddenmagic:morph_phase"},
                ],
            },
        })
    print(f"morph tools: {len(MORPH_TEXTURES)} two-layer models")


def generate_mob_crystal() -> None:
    write_json(ASSETS / "models" / "item" / "mob_crystal_empty.json", {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": "forbiddenmagic:item/emptycrystal"},
    })
    write_json(ASSETS / "models" / "item" / "mob_crystal_filled.json", {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": "forbiddenmagic:item/mobcrystal"},
    })
    write_json(ASSETS / "items" / "mob_crystal.json", {
        "model": {
            "type": "minecraft:condition",
            "property": "minecraft:has_component",
            "component": "forbiddenmagic:mob_id",
            "on_true": {"type": "minecraft:model", "model": "forbiddenmagic:item/mob_crystal_filled"},
            "on_false": {"type": "minecraft:model", "model": "forbiddenmagic:item/mob_crystal_empty"},
        },
    })
    print("mob crystal: 2 models and one condition definition")


def generate_two_layer_items() -> None:
    for name, overlay in sorted(TWO_LAYER_TEXTURES.items()):
        write_json(ASSETS / "models" / "item" / f"{name}.json", {
            "parent": "minecraft:item/generated",
            "textures": {
                "layer0": f"forbiddenmagic:item/{name}",
                "layer1": f"forbiddenmagic:item/{overlay}",
            },
        })
        write_json(ASSETS / "items" / f"{name}.json", {
            "model": {"type": "minecraft:model", "model": f"forbiddenmagic:item/{name}"},
        })
    print(f"two-layer items: {len(TWO_LAYER_TEXTURES)} models and definitions")



BLOCK_TEXTURES = {
    "taint_planks": ("taint_planks",),
    "taint_stone_smooth": ("taint_stone_smooth",),
    "taint_stone_brick": ("taint_stone_brick",),
    "star_block": ("starblock",),
}


def generate_blocks() -> None:
    for name, (texture,) in sorted(BLOCK_TEXTURES.items()):
        write_json(ASSETS / "blockstates" / f"{name}.json", {"variants": {"": {"model": f"forbiddenmagic:block/{name}"}}})
        write_json(ASSETS / "models" / "block" / f"{name}.json", {
            "parent": "minecraft:block/cube_all",
            "textures": {"all": f"forbiddenmagic:block/{texture}"},
        })
        write_json(ASSETS / "models" / "item" / f"{name}.json", {"parent": f"forbiddenmagic:block/{name}"})
        write_json(ASSETS / "items" / f"{name}.json", {
            "model": {"type": "minecraft:model", "model": f"forbiddenmagic:block/{name}"},
        })
    write_json(ASSETS / "blockstates" / "taint_log.json", {
        "variants": {
            "axis=x": {"type": "forbiddenmagic:taint_log_faces"},
            "axis=y": {"type": "forbiddenmagic:taint_log_faces"},
            "axis=z": {"type": "forbiddenmagic:taint_log_faces"},
        }
    })
    write_json(ASSETS / "models" / "block" / "taint_log.json", {
        "parent": "minecraft:block/cube_column",
        "textures": {"end": "forbiddenmagic:block/taint_log_bottom", "side": "forbiddenmagic:block/taint_log_side"},
    })
    write_json(ASSETS / "models" / "block" / "taint_log_horizontal.json", {
        "parent": "minecraft:block/cube_column_horizontal",
        "textures": {"end": "forbiddenmagic:block/taint_log_bottom", "side": "forbiddenmagic:block/taint_log_side"},
    })
    for gross in ("gross", "gross2"):
        write_json(ASSETS / "models" / "block" / f"taint_log_{gross}.json", {
            "parent": "minecraft:block/cube_column",
            "textures": {"end": "forbiddenmagic:block/taint_log_bottom", "side": f"forbiddenmagic:block/taint_log_side_{gross}"},
        })
        write_json(ASSETS / "models" / "block" / f"taint_log_{gross}_horizontal.json", {
            "parent": "minecraft:block/cube_column_horizontal",
            "textures": {"end": "forbiddenmagic:block/taint_log_bottom", "side": f"forbiddenmagic:block/taint_log_side_{gross}"},
        })
    write_json(ASSETS / "models" / "item" / "taint_log.json", {"parent": "forbiddenmagic:block/taint_log"})
    write_json(ASSETS / "items" / "taint_log.json", {
        "model": {"type": "minecraft:model", "model": "forbiddenmagic:block/taint_log"},
    })
    print(f"blocks: {len(BLOCK_TEXTURES) + 1} models and blockstates")


PLANT_MODELS = {
    "taint_leaves": ("cube_all", "taint_leaves"),
    "taint_sapling": ("cross", "sapling_taint"),
}


def generate_plants() -> None:
    for name, (parent, texture) in sorted(PLANT_MODELS.items()):
        variants = (
            {"stage=0": {"model": f"forbiddenmagic:block/{name}"},
             "stage=1": {"model": f"forbiddenmagic:block/{name}"}}
            if name == "taint_sapling"
            else {"": {"model": f"forbiddenmagic:block/{name}"}}
        )
        write_json(ASSETS / "blockstates" / f"{name}.json", {"variants": variants})
        model = {
            "parent": f"minecraft:block/{parent}",
            "render_type": "minecraft:cutout",
        }
        if parent == "cube_all":
            model["textures"] = {"all": f"forbiddenmagic:block/{texture}"}
        else:
            model["textures"] = {"cross": f"forbiddenmagic:block/{texture}"}
        write_json(ASSETS / "models" / "block" / f"{name}.json", model)
        write_json(ASSETS / "models" / "item" / f"{name}.json", {"parent": f"forbiddenmagic:block/{name}"})
        write_json(ASSETS / "items" / f"{name}.json", {
            "model": {"type": "minecraft:model", "model": f"forbiddenmagic:block/{name}"},
        })
    print(f"plants: {len(PLANT_MODELS)} models and blockstates")


def generate_umbral_plants() -> None:
    write_json(ASSETS / "blockstates" / "umbral_rose.json", {"variants": {"": {"model": "forbiddenmagic:block/umbral_rose"}}})
    write_json(ASSETS / "models" / "block" / "umbral_rose.json", {
        "parent": "minecraft:block/cross",
        "render_type": "minecraft:cutout",
        "textures": {"cross": "forbiddenmagic:block/flower_black"},
    })
    write_json(ASSETS / "models" / "item" / "umbral_rose.json", {"parent": "forbiddenmagic:block/umbral_rose"})
    write_json(ASSETS / "items" / "umbral_rose.json", {
        "model": {"type": "minecraft:model", "model": "forbiddenmagic:block/umbral_rose"},
    })
    write_json(ASSETS / "blockstates" / "umbral_rose_bush.json", {
        "variants": {
            "half=lower": {"model": "forbiddenmagic:block/umbral_rose_bush_bottom"},
            "half=upper": {"model": "forbiddenmagic:block/umbral_rose_bush_top"},
        }
    })
    for half, texture in (("bottom", "umbral_rose_bottom"), ("top", "umbral_rose_top")):
        write_json(ASSETS / "models" / "block" / f"umbral_rose_bush_{half}.json", {
            "parent": "minecraft:block/cross",
            "render_type": "minecraft:cutout",
            "textures": {"cross": f"forbiddenmagic:block/{texture}"},
        })
    write_json(ASSETS / "models" / "item" / "umbral_rose_bush.json", {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": "forbiddenmagic:block/umbral_rose_top"},
    })
    write_json(ASSETS / "items" / "umbral_rose_bush.json", {
        "model": {"type": "minecraft:model", "model": "forbiddenmagic:item/umbral_rose_bush"},
    })
    print("umbral plants: 4 models and 2 blockstates")


def generate_wrath_cage() -> None:
    write_json(ASSETS / "blockstates" / "wrath_cage.json", {"variants": {"": {"model": "forbiddenmagic:block/wrath_cage"}}})
    write_json(ASSETS / "models" / "block" / "wrath_cage.json", {
        "parent": "minecraft:block/cube_all",
        "render_type": "minecraft:cutout",
        "textures": {"all": "forbiddenmagic:block/spirit_box"},
    })
    write_json(ASSETS / "models" / "item" / "wrath_cage.json", {"parent": "forbiddenmagic:block/wrath_cage"})
    write_json(ASSETS / "items" / "wrath_cage.json", {
        "model": {"type": "minecraft:model", "model": "forbiddenmagic:block/wrath_cage"},
    })
    print("wrath cage: one model and blockstate")


def generate_cake() -> None:
    textures = {
        "particle": "forbiddenmagic:block/cake_side",
        "top": "forbiddenmagic:block/cake_top",
        "bottom": "forbiddenmagic:block/cake_bottom",
        "side": "forbiddenmagic:block/cake_side",
        "inner": "forbiddenmagic:block/cake_inner",
    }
    variants = {}
    for slice_index in range(12):
        x0 = min((1 + slice_index) / 12 * 16, 14.0)
        name = f"arcane_cake_{slice_index}"
        write_json(ASSETS / "models" / "block" / f"{name}.json", {
            "parent": "minecraft:block/block",
            "textures": textures,
            "elements": [{
                "from": [round(x0, 3), 0, 1],
                "to": [15, 8, 15],
                "faces": {
                    "down": {"texture": "#bottom", "cullface": "down"},
                    "up": {"texture": "#top"},
                    "north": {"texture": "#side"},
                    "south": {"texture": "#side"},
                    "east": {"texture": "#side"},
                    "west": {"texture": "#inner"},
                },
            }],
        })
        variants[f"slice={slice_index}"] = {"model": f"forbiddenmagic:block/{name}"}
    write_json(ASSETS / "blockstates" / "arcane_cake.json", {"variants": variants})
    write_json(ASSETS / "models" / "item" / "arcane_cake.json", {"parent": "forbiddenmagic:block/arcane_cake_0"})
    write_json(ASSETS / "items" / "arcane_cake.json", {
        "model": {"type": "minecraft:model", "model": "forbiddenmagic:block/arcane_cake_0"},
    })
    print("cake: 12 slice models and one blockstate")

def main() -> int:
    generate_lang()
    generate_items()
    generate_morph_items()
    generate_mob_crystal()
    generate_two_layer_items()
    generate_blocks()
    generate_plants()
    generate_umbral_plants()
    generate_cake()
    generate_wrath_cage()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
