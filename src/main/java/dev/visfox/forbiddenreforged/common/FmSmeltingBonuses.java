package dev.visfox.forbiddenreforged.common;

import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import net.minecraft.world.item.Items;

/**
 * Registers Forbidden Magic's original smelting bonuses with the host.
 *
 * <p>The original add-on granted an emerald smelting bonus through the
 * host's {@code addSmeltingBonus} API. Hosts that keep that table internal
 * expose no registration method, so the bonus stays dormant: the ported
 * crafting conversions remain the only emerald/fragment conversion, and no
 * substitute mechanic is used.
 *
 * <p>Registration runs after host data components are bound, because a host
 * API that accepts item stacks reads their components.
 */
public final class FmSmeltingBonuses {
    private static boolean registered;

    private FmSmeltingBonuses() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        var bonuses = HostAdapterRegistry.smeltingBonuses();
        if (!bonuses.supportsBonuses()) {
            FmIds.LOGGER.info("Host exposes no smelting-bonus API; the emerald bonus stays inactive");
            return;
        }
        if (bonuses.registerBonus(Items.EMERALD, FmItems.EMERALD_FRAGMENT.get())) {
            FmIds.LOGGER.info("Registered the emerald smelting bonus");
        }
    }
}
