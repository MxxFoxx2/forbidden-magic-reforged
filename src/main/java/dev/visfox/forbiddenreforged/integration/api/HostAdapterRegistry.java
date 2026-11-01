package dev.visfox.forbiddenreforged.integration.api;

import dev.visfox.forbiddenreforged.integration.runtime.HostItemFactoryServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostItemPropertyServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostBootstrapServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostEntityAspectServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostEntityClassificationServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostAspectServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostMorphToolServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostBlockEntityServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWandServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostTaintServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostScribingServices;
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

    /** Returns the selected host's typed entity-to-aspect catalog. */
    public static HostEntityAspectServices requireEntityAspects() {
        return capability(HostEntityAspectServices.class, "entity aspect");
    }

    /** Returns host-specific entity classification predicates and boss identifiers. */
    public static HostEntityClassificationServices requireEntityClassification() {
        return capability(HostEntityClassificationServices.class, "entity classification");
    }

    public static HostAspectServices requireAspects() {
        return capability(HostAspectServices.class, "aspect");
    }

    public static HostMorphToolServices requireMorphTools() {
        return capability(HostMorphToolServices.class, "morph tool");
    }

    public static HostBlockEntityServices requireBlockEntities() {
        return capability(HostBlockEntityServices.class, "block entity");
    }

    public static HostWandServices requireWands() {
        return capability(HostWandServices.class, "wand");
    }

    public static HostTaintServices requireTaint() {
        return capability(HostTaintServices.class, "taint");
    }

    public static HostScribingServices requireScribing() {
        return capability(HostScribingServices.class, "scribing");
    }

    private static <T> T capability(Class<T> type, String name) {
        HostAdapter adapter = require();
        if (type.isInstance(adapter)) {
            return type.cast(adapter);
        }
        throw new IllegalStateException(
            "Selected host adapter does not expose " + name + " services: " + adapter.profile().id()
        );
    }

    /** Returns host-specific Item.Properties customizations. */
    public static HostItemPropertyServices requireItemProperties() {
        return capability(HostItemPropertyServices.class, "item properties");
    }

    /** Returns host-backed Item implementations. */
    public static HostItemFactoryServices requireItemFactories() {
        return capability(HostItemFactoryServices.class, "item factories");
    }

    /**
     * Returns host-specific startup, trigger, and creative-tab services.
     */
    public static HostBootstrapServices requireBootstrap() {
        return capability(HostBootstrapServices.class, "bootstrap");
    }
}
