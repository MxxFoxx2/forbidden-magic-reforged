from .context import ASSETS, write_json


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
