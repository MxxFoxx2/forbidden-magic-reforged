package dev.visfox.forbiddenreforged.integration.runtime;

import net.minecraft.world.item.Item;

/**
 * Host support for item smelting bonuses: a furnace that smelts {@code input}
 * may additionally yield {@code bonus}.
 *
 * <p>The capability is dormant until the host exposes an API to register such
 * bonuses. Hosts without that API must report the bonus as unavailable instead
 * of approximating it with a different mechanic.
 */
public interface HostSmeltingBonusServices {
    /**
     * Registers a smelting bonus for {@code input}.
     *
     * @return {@code true} when the host accepted the bonus, {@code false} when
     *     the host has no API for registering smelting bonuses
     */
    boolean registerBonus(Item input, Item bonus);

    /**
     * @return {@code true} when the host exposes an API for registering smelting
     *     bonuses, so {@link #registerBonus(Item, Item)} can succeed
     */
    boolean supportsBonuses();
}
