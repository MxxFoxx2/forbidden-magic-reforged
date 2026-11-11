package dev.visfox.forbiddenreforged.core.wand;

import net.minecraft.world.entity.player.Player;

/**
 * Bridge to an external mana economy so rod updates stay host-neutral.
 */
public interface ManaSource {
    boolean available();

    double mana(Player player);

    boolean spend(Player player, double amount);
}
