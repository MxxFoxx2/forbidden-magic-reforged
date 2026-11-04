package dev.visfox.forbiddenreforged.integration.tc4;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import thaumcraft.item.ScribingToolsItem;

/**
 * TC4 scribing-table item shell for the permanent-use Primewell.
 */
public final class Tc4PrimewellItem extends ScribingToolsItem {
    public Tc4PrimewellItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;
    }
}
