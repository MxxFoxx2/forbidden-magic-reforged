package dev.visfox.forbiddenreforged.integration.tc4;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.platform.Baubles;

public final class Tc4AccessoryAdapter {
    private Tc4AccessoryAdapter() {
    }

    public static ItemStack getBauble(Player player, int slot) {
        return Baubles.get(player, slot);
    }

    public static void setBauble(Player player, int slot, ItemStack stack) {
        Baubles.set(player, slot, stack);
    }
}
