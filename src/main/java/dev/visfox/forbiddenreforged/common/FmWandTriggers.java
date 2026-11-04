package dev.visfox.forbiddenreforged.common;

import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;

public final class FmWandTriggers {
    private FmWandTriggers() {
    }

    public static void register() {
        HostAdapterRegistry.requireBootstrap().registerWandTriggers();
    }
}
