package dev.visfox.forbiddenreforged.integration.runtime;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Wand, focus, Vis-storage, and accessory capabilities of a magic host. */
public interface HostWandServices {
    boolean isWand(ItemStack stack);

    String wandRodTag(ItemStack stack);

    int wandFocusTreasure(ItemStack stack);

    int wandFocusPotency(ItemStack stack);

    void addWandVis(ItemStack stack, ContentId aspect, int amount, boolean apply);

    void addRealVis(ItemStack stack, ContentId aspect, int amount, boolean apply);

    ItemStack accessory(Player player, int slot);
}
