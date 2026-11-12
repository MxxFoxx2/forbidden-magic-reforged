package dev.visfox.forbiddenreforged.integration.api;

import dev.visfox.forbiddenreforged.integration.runtime.HostItemFactoryServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostItemPropertyServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWandRegistrationServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostOptionalEnchantmentServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostCreativeContentServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostEntityAspectServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostEntityClassificationServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostAspectServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostMorphToolServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostBlockEntityServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWandInspectionServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWandVisServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWandPartsServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostFocusServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostEssentiaTransportServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostAccessoryServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostTaintBlockServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostTaintOriginServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostEnchantmentEffectServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostScribingServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWarpServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostVillagerTradeServices;
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

    public static HostWandInspectionServices requireWandInspection() {
        return capability(HostWandInspectionServices.class, "wand inspection");
    }

    public static HostWandVisServices requireWandVis() {
        return capability(HostWandVisServices.class, "wand Vis");
    }

    public static HostWandPartsServices requireWandParts() {
        return capability(HostWandPartsServices.class, "wand parts");
    }

    public static HostFocusServices requireFocus() {
        return capability(HostFocusServices.class, "focus");
    }

    public static HostEssentiaTransportServices requireEssentiaTransport() {
        return capability(HostEssentiaTransportServices.class, "essentia transport");
    }

    public static HostAccessoryServices requireAccessories() {
        return capability(HostAccessoryServices.class, "accessory");
    }

    public static HostTaintBlockServices requireTaintBlocks() {
        return capability(HostTaintBlockServices.class, "taint block");
    }

    public static HostTaintOriginServices requireTaintOrigins() {
        return capability(HostTaintOriginServices.class, "taint origins");
    }

    public static HostEnchantmentEffectServices requireEnchantmentEffects() {
        return capability(HostEnchantmentEffectServices.class, "enchantment effect");
    }

    public static HostScribingServices requireScribing() {
        return capability(HostScribingServices.class, "scribing");
    }

    public static HostWarpServices requireWarp() {
        return capability(HostWarpServices.class, "warp");
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

    /** Returns host-specific wand material and trigger registration. */
    public static HostWandRegistrationServices requireWandRegistration() {
        return capability(HostWandRegistrationServices.class, "wand registration");
    }

    /** Returns the optional enchantment bridge when the selected host supports it. */
    public static HostOptionalEnchantmentServices requireOptionalEnchantments() {
        return capability(HostOptionalEnchantmentServices.class, "optional enchantments");
    }

    /** Returns host-backed content contributed to the shared creative tab. */
    public static HostCreativeContentServices requireCreativeContent() {
        return capability(HostCreativeContentServices.class, "creative content");
    }

    /** Returns host-specific trades for the Forbidden Magic heretic profession. */
    public static HostVillagerTradeServices requireVillagerTrades() {
        return capability(HostVillagerTradeServices.class, "villager trades");
    }
}
