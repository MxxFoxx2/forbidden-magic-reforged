package dev.visfox.forbiddenreforged.common;

import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;

public final class FmWands {
    private FmWands() {
    }

    public static void register() {
        HostAdapterRegistry.bootstrap().registerWands();
    }
}
