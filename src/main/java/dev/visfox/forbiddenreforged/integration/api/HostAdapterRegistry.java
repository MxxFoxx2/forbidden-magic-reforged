package dev.visfox.forbiddenreforged.integration.api;

import dev.visfox.forbiddenreforged.integration.runtime.HostRuntimeServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostItemServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostBootstrapServices;
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

    /**
     * Returns the Minecraft-runtime capabilities of the selected host.
     */
    public static HostRuntimeServices requireRuntime() {
        HostAdapter adapter = require();
        if (adapter instanceof HostRuntimeServices runtime) {
            return runtime;
        }
        throw new IllegalStateException(
            "Selected host adapter does not expose runtime services: " + adapter.profile().id()
        );
    }

    /**
     * Returns host-specific item factories and item property adapters.
     */
    public static HostItemServices requireItems() {
        HostAdapter adapter = require();
        if (adapter instanceof HostItemServices items) {
            return items;
        }
        throw new IllegalStateException(
            "Selected host adapter does not expose item services: " + adapter.profile().id()
        );
    }

    /**
     * Returns host-specific startup, trigger, and creative-tab services.
     */
    public static HostBootstrapServices requireBootstrap() {
        HostAdapter adapter = require();
        if (adapter instanceof HostBootstrapServices bootstrap) {
            return bootstrap;
        }
        throw new IllegalStateException(
            "Selected host adapter does not expose bootstrap services: " + adapter.profile().id()
        );
    }
}
