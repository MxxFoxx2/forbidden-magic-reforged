package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;

/** Compatibility facade for the optional Thaumic Tinkerer integration. */
public final class FmTinkerer {
    private FmTinkerer() {
    }

    public static void register() {
        HostAdapterRegistry.requireOptionalEnchantments().registerOptionalEnchantments();
    }

    public static int registeredCount() {
        return HostAdapterRegistry.requireOptionalEnchantments().registeredOptionalEnchantments();
    }
}
