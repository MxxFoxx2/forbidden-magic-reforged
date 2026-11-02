package dev.visfox.forbiddenreforged.integration.runtime;

import java.util.List;
import net.minecraft.world.item.ItemStack;

/**
 * Host-specific startup registrations and host-backed creative-tab entries.
 *
 * The shared mod lifecycle depends on this capability instead of importing
 * concrete host wand, focus, or optional-addon APIs.
 */
public interface HostBootstrapServices {
    void registerWands();

    void registerWandTriggers();

    void registerOptionalEnchantments();

    int registeredOptionalEnchantments();

    boolean isArsMagicaLoaded();

    void addCreativeWands(List<ItemStack> stacks);
}
