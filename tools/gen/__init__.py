from . import blocks, items, lang, structures


def main() -> int:
    lang.generate_lang()
    items.generate_items()
    items.generate_morph_items()
    items.generate_mob_crystal()
    items.generate_two_layer_items()
    blocks.generate_blocks()
    blocks.generate_plants()
    blocks.generate_umbral_plants()
    structures.generate_cake()
    structures.generate_wrath_cage()
    return 0
