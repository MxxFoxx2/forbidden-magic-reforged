package dev.visfox.forbiddenreforged.core.wand;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface WandTriggerAccess {
    boolean isWand(ItemStack wand);

    boolean hasFocus(ItemStack wand);

    boolean researchComplete(Player player, ContentId research);

    boolean consumeVis(ItemStack wand, Player player, ContentId aspect, int amount);
}
