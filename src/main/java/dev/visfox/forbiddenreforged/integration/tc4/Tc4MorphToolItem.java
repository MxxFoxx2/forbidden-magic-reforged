package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.FmEnchantments;
import dev.visfox.forbiddenreforged.MorphToolItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.item.gear.WarpingGear;

public final class Tc4MorphToolItem extends MorphToolItem implements WarpingGear {
    public Tc4MorphToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {
        return FmEnchantments.level(player.level().registryAccess(), stack, FmEnchantments.VOIDTOUCHED) > 0 ? 1 : 0;
    }
}
