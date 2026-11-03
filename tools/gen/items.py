from .context import ASSETS, TEXTURES, write_json


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
