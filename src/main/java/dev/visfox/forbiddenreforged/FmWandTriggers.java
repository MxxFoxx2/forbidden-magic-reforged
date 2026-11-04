package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;

/** Compatibility facade for the selected host's wand trigger registration. */
public final class FmWandTriggers {
    private FmWandTriggers() {
    }

    public static void register() {
        HostAdapterRegistry.requireWandRegistration().registerWandTriggers();
    }
}
