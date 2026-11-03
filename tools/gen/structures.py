from .context import ASSETS, write_json


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
