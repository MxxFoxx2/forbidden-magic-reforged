package dev.visfox.forbiddenreforged.core.focus;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface BlinkFocusAccess {
    boolean consumeVis(ItemStack wand, Player player, boolean apply);

    boolean hasUpgrade(ItemStack wand, ContentId upgrade);

    int potency(ItemStack wand);

    int enlarge(ItemStack wand);
}
