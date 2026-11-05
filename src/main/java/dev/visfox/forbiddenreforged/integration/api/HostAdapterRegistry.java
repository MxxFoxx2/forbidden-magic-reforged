package dev.visfox.forbiddenreforged.integration.api;

import dev.visfox.forbiddenreforged.integration.runtime.HostAccessoryServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostAspectServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostBlockEntityServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostBootstrapServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostEnchantmentEffectServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostEntityAspectServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostEntityClassificationServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostItemFactoryServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostItemPropertyServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostMorphToolServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostScribingServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostSmeltingBonusServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostTaintBlockServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWandInspectionServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWandVisServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWarpServices;
import java.util.Objects;

/**
 * Holds the host adapter selected by the mod's composition root.
 *
 * Shared code depends on the HostAdapter contract rather than importing a
 * concrete host implementation. The selected adapter is installed exactly
 * once during mod construction, before catalog registration begins; runtime
 * ports are read from its immutable {@link HostServices} composition.
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

    /** The selected host's typed runtime capability composition. */
    public static HostServices services() {
        return require().services();
    }

    public static HostEntityAspectServices entityAspects() {
        return services().entityAspects();
    }

    public static HostEntityClassificationServices entityClassification() {
        return services().entityClassification();
    }

    public static HostAspectServices aspects() {
        return services().aspects();
    }

    public static HostMorphToolServices morphTools() {
        return services().morphTools();
    }

    public static HostBlockEntityServices blockEntities() {
        return services().blockEntities();
    }

    public static HostWandInspectionServices wandInspection() {
        return services().wandInspection();
    }

    public static HostWandVisServices wandVis() {
        return services().wandVis();
    }

    public static HostAccessoryServices accessories() {
        return services().accessories();
    }

    public static HostTaintBlockServices taintBlocks() {
        return services().taintBlocks();
    }

    public static HostEnchantmentEffectServices enchantmentEffects() {
        return services().enchantmentEffects();
    }

    public static HostScribingServices scribing() {
        return services().scribing();
    }

    public static HostWarpServices warp() {
        return services().warp();
    }

    public static HostItemPropertyServices itemProperties() {
        return services().itemProperties();
    }

    public static HostItemFactoryServices itemFactories() {
        return services().itemFactories();
    }

    public static HostBootstrapServices bootstrap() {
        return services().bootstrap();
    }

    public static HostSmeltingBonusServices smeltingBonuses() {
        return services().smeltingBonuses();
    }
}
