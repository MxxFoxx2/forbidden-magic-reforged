package dev.visfox.forbiddenreforged.integration.api;

import java.util.Objects;

/**
 * Holds the host adapter selected by the mod's composition root.
 *
 * Core catalogs depend on the HostAdapter contract rather than importing a
 * concrete host implementation. The selected adapter is installed exactly
 * once during mod construction, before catalog registration begins.
 */
public final class HostAdapterRegistry {
    private static volatile HostAdapter active;

    private HostAdapterRegistry() {
    }

    public static synchronized void install(HostAdapter adapter) {
        Objects.requireNonNull(adapter, "adapter");
        if (active != null && active != adapter) {
            throw new IllegalStateException(
                "A host adapter is already installed: " + active.profile().id()
            );
        }
        active = adapter;
    }

    public static HostAdapter require() {
        HostAdapter adapter = active;
        if (adapter == null) {
            throw new IllegalStateException(
                "No host adapter has been installed before content registration"
            );
        }
        return adapter;
    }
}
