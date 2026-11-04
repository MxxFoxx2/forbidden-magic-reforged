package dev.visfox.forbiddenreforged.common;

import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;

/** Compatibility entry point for the optional Thaumic Tinkerer integration. */
public final class FmTinkerer {
    private FmTinkerer() {
    }

    public static void register() {
        HostAdapterRegistry.bootstrap().registerOptionalEnchantments();
    }

    public static int registeredCount() {
        return HostAdapterRegistry.bootstrap().registeredOptionalEnchantments();
    }
}
