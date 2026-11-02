package dev.visfox.forbiddenreforged.integration.runtime;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Access to optional host accessory slots without exposing the accessory API. */
public interface HostAccessoryServices {
    ItemStack accessory(Player player, int slot);
}
