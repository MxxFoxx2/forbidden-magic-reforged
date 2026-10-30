#!/usr/bin/env python3
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "src" / "thaumaturge" / "resources"
HOST = ROOT.parent / "Thaumaturge"
ASSETS = ROOT / "src" / "main" / "resources" / "assets"

SHARDS = ["wrath", "envy", "taint", "pride", "lust", "sloth", "greed", "gluttony"]
EXTRA_ITEMS = ["emerald_fragment", "taint_coal", "taint_fruit", "black_dye", "diabolist_fork", "taint_shovel", "taint_pickaxe",
               "mob_crystal", "skull_axe", "riding_crop", "ring_nutrition", "crystalwell"]
ITEM_MODEL_PARTS = {"mob_crystal": ["mob_crystal_empty", "mob_crystal_filled"]}
BLOCKS = ["taint_log", "taint_planks", "taint_stone_smooth", "taint_stone_brick", "star_block", "taint_leaves", "taint_sapling",
          "umbral_rose", "umbral_rose_bush", "arcane_cake", "wrath_cage"]
NO_LOOT = {"arcane_cake", "wrath_cage"}
MODEL_PARTS = {
    "umbral_rose_bush": ["umbral_rose_bush_bottom", "umbral_rose_bush_top"],
    "arcane_cake": ["arcane_cake_" + str(slice) for slice in range(12)],
}
ASPECTS = ["luxuria", "infernus", "superbia", "gula", "invidia", "desidia", "ira"]


def fail(message):
    print("FAIL " + message)
    return 1


def load(path):
    with path.open(encoding="utf-8") as handle:
        return json.load(handle)


def host_aspects():
    source = (HOST / "src/main/java/com/leclowndu93150/thaumaturge/api/aspect/TTAspects.java").read_text(encoding="utf-8")
    return set(re.findall(r'IAspect>\s+[A-Z_]+\s*=\s*key\("([a-z_]+)"\)', source))


def texture_exists(identifier):
    namespace, path = identifier.split(":", 1)
    if namespace != "forbiddenmagic":
        return True
    candidates = [
        RES / "assets" / namespace / path,
        ASSETS / namespace / path,
        RES / "assets" / namespace / "textures" / (path + ".png"),
        ASSETS / namespace / "textures" / (path + ".png"),
    ]
    return any(candidate.is_file() for candidate in candidates)


def main():
    errors = 0
    known_aspects = host_aspects()
    if len(known_aspects) < 30:
        errors += fail("could not read Thaumaturge aspect tags")
    lang_en = load(RES / "assets/forbiddenmagic/lang/en_us.json")
    lang_ru = load(RES / "assets/forbiddenmagic/lang/ru_ru.json")

    aspect_dir = RES / "data/forbiddenmagic/thaumaturge/aspect"
    seen = set()
    for path in sorted(aspect_dir.glob("*.json")):
        data = load(path)
        tag = path.stem
        seen.add(tag)
        if data.get("tag") != tag:
            errors += fail(f"{path.name}: tag mismatch")
        color = data.get("color")
        if not isinstance(color, int) or not 0 <= color <= 0xFFFFFF:
            errors += fail(f"{path.name}: bad color")
        components = data.get("components", [])
        if len(components) != 2:
            errors += fail(f"{path.name}: needs exactly two components")
        for component in components:
            namespace, _, name = component.partition(":")
            if namespace != "thaumaturge" or name not in known_aspects:
                errors += fail(f"{path.name}: unknown host component {component}")
        texture = data.get("texture")
        if texture and not texture_exists(texture):
            errors += fail(f"{path.name}: missing texture {texture}")
        if f"aspect.forbiddenmagic.{tag}" not in lang_en or f"aspect.forbiddenmagic.{tag}" not in lang_ru:
            errors += fail(f"{path.name}: missing localized name")
    for tag in ASPECTS:
        if tag not in seen:
            errors += fail(f"missing aspect file {tag}.json")

    category_dir = RES / "data/forbiddenmagic/thaumaturge/research_category"
    categories = set()
    for path in sorted(category_dir.glob("*.json")):
        data = load(path)
        categories.add("forbiddenmagic:" + path.stem)
        for entry in data.get("formula", []):
            aspect = entry.get("aspect", "")
            namespace, _, name = aspect.partition(":")
            if namespace == "thaumaturge" and name not in known_aspects:
                errors += fail(f"{path.name}: unknown formula aspect {aspect}")
            if namespace == "forbiddenmagic" and name not in ASPECTS:
                errors += fail(f"{path.name}: unknown formula aspect {aspect}")
        for field in ("icon", "background", "overlay_background"):
            value = data.get(field)
            if value and not texture_exists(value):
                errors += fail(f"{path.name}: missing texture {value}")
        key = "research_category." + "forbiddenmagic." + path.stem
        if key not in lang_en or key not in lang_ru:
            errors += fail(f"{path.name}: missing {key}")

    entry_dir = RES / "data/forbiddenmagic/thaumaturge/research_entry"
    for path in sorted(entry_dir.glob("*.json")):
        data = load(path)
        if data.get("category") not in categories:
            errors += fail(f"{path.name}: unknown category {data.get('category')}")
        name = data.get("name")
        if name not in lang_en or name not in lang_ru:
            errors += fail(f"{path.name}: missing {name}")
        for stage in data.get("stages", []):
            text = stage.get("text")
            if text not in lang_en or text not in lang_ru:
                errors += fail(f"{path.name}: missing {text}")
        for icon in data.get("icons", []):
            if icon.endswith(".png"):
                if not texture_exists(icon):
                    errors += fail(f"{path.name}: missing icon texture {icon}")
                continue
            namespace, _, item = icon.partition(":")
            if namespace == "forbiddenmagic":
                if not (RES / "assets/forbiddenmagic/items" / (item + ".json")).is_file():
                    errors += fail(f"{path.name}: unknown item icon {icon}")

    for shard in SHARDS:
        item = shard + "_shard"
        definition = RES / "assets/forbiddenmagic/items" / (item + ".json")
        model = RES / "assets/forbiddenmagic/models/item" / (item + ".json")
        if not definition.is_file():
            errors += fail(f"missing item definition {item}")
        if not model.is_file():
            errors += fail(f"missing item model {item}")
        texture = ASSETS / "forbiddenmagic/textures/item" / (item + ".png")
        if not texture.is_file():
            errors += fail(f"missing item texture {item}")
        key = "item.forbiddenmagic." + item
        if key not in lang_en or key not in lang_ru:
            errors += fail(f"missing {key}")

    for item in EXTRA_ITEMS:
        definition = RES / "assets/forbiddenmagic/items" / (item + ".json")
        models = [RES / "assets/forbiddenmagic/models/item" / (name + ".json")
                  for name in ITEM_MODEL_PARTS.get(item, [item])]
        if not definition.is_file():
            errors += fail(f"missing item definition {item}")
        for model in models:
            if not model.is_file():
                errors += fail(f"missing item model {model.stem}")
        key = "item.forbiddenmagic." + item
        if item == "mob_crystal":
            for suffix in (".empty", ".filled"):
                if key + suffix not in lang_en or key + suffix not in lang_ru:
                    errors += fail(f"missing {key}{suffix}")
        elif key not in lang_en or key not in lang_ru:
            errors += fail(f"missing {key}")

    for block in BLOCKS:
        state = RES / "assets/forbiddenmagic/blockstates" / (block + ".json")
        models = [RES / "assets/forbiddenmagic/models/block" / (name + ".json") for name in MODEL_PARTS.get(block, [block])]
        definition = RES / "assets/forbiddenmagic/items" / (block + ".json")
        paths = [state, definition, *models]
        if block not in NO_LOOT:
            paths.append(RES / "data/forbiddenmagic/loot_table/blocks" / (block + ".json"))
        for path in paths:
            if not path.is_file():
                errors += fail(f"missing block file {path.relative_to(RES)}")
        for model in models:
            if model.is_file():
                for texture in load(model).get("textures", {}).values():
                    if not texture_exists(texture):
                        errors += fail(f"{block}: missing texture {texture}")
        key = "block.forbiddenmagic." + block
        if key not in lang_en or key not in lang_ru:
            errors += fail(f"missing {key}")

    recipe_dir = RES / "data/forbiddenmagic/recipe"
    entry_ids = {"forbiddenmagic:" + path.stem for path in entry_dir.glob("*.json")}
    for path in sorted(recipe_dir.glob("*.json")):
        data = load(path)
        result = data.get("result", {})
        result_id = result.get("id") if isinstance(result, dict) else None
        if result_id and result_id.startswith("forbiddenmagic:"):
            item = result_id.split(":", 1)[1]
            if not (RES / "assets/forbiddenmagic/items" / (item + ".json")).is_file():
                errors += fail(f"{path.name}: unknown result {result_id}")
        for aspect in data.get("aspects", []):
            namespace, _, name = aspect.get("aspect", "").partition(":")
            if namespace == "thaumaturge" and name not in known_aspects:
                errors += fail(f"{path.name}: unknown recipe aspect {aspect.get('aspect')}")
            if namespace == "forbiddenmagic" and name not in ASPECTS:
                errors += fail(f"{path.name}: unknown recipe aspect {aspect.get('aspect')}")
        gate = data.get("research")
        if isinstance(gate, dict) and gate.get("entry") not in entry_ids:
            errors += fail(f"{path.name}: unknown research gate {gate.get('entry')}")

    for enchantment in ["wrath", "greedy", "consuming", "educational", "corrupting", "cluster", "impact", "voidtouched"]:
        path = RES / "data/forbiddenmagic/enchantment" / (enchantment + ".json")
        if not path.is_file():
            errors += fail(f"missing enchantment {enchantment}")
        key = "enchantment.forbiddenmagic." + enchantment
        if key not in lang_en or key not in lang_ru:
            errors += fail(f"missing {key}")

    for key in sorted(set(lang_en) | set(lang_ru)):
        if key not in lang_en or key not in lang_ru:
            errors += fail(f"lang key missing on one side: {key}")

    if errors:
        print(f"{errors} problem(s)")
        return 1
    print("Thaumaturge data check passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
