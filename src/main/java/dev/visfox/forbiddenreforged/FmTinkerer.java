package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;

/** Compatibility entry point for the optional Thaumic Tinkerer integration. */
public final class FmTinkerer {
    private FmTinkerer() {
    }

    public static void register() {
        HostAdapterRegistry.requireBootstrap().registerOptionalEnchantments();
    }

    public static int registeredCount() {
        return HostAdapterRegistry.requireBootstrap().registeredOptionalEnchantments();
    }
}
