package dev.visfox.forbiddenreforged;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public final class FmMaterials {
    public static final ToolMaterial THAUMIUM = new ToolMaterial(
        BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
        500,
        7.0F,
        2.5F,
        22,
        TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/thaumium"))
    );

    public static final ToolMaterial ELEMENTAL = new ToolMaterial(
        BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
        1500,
        9.0F,
        3.0F,
        18,
        TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/thaumium"))
    );

    private FmMaterials() {
    }
}
