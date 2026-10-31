package dev.visfox.forbiddenreforged;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import thaumcraft.item.ScribingToolsItem;

/**
 * Original Forbidden Magic Primewell: an epic scribing tool whose charge is
 * never consumed. The table API checks ScribingToolsItem and wears stacks
 * directly, so an effectively unbounded max damage plus a hidden bar preserves
 * the original permanent-use behavior without requiring a host patch.
 */
public final class PrimewellItem extends ScribingToolsItem {
    public PrimewellItem(Properties properties) {
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
