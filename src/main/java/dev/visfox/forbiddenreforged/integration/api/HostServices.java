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
import dev.visfox.forbiddenreforged.integration.runtime.HostTaintBlockServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWandInspectionServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWandVisServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWarpServices;
import java.util.Objects;

/**
 * Immutable, typed composition of the host's runtime capability ports.
 *
 * A {@link HostAdapter} exposes exactly one HostServices instance; consumers
 * read the port they need ({@code services.warp().changeWarp(...)}) instead of
 * doing instanceof checks against a single god adapter.
 */
public record HostServices(
    HostAspectServices aspects,
    HostEntityAspectServices entityAspects,
    HostEntityClassificationServices entityClassification,
    HostMorphToolServices morphTools,
    HostBlockEntityServices blockEntities,
    HostWandInspectionServices wandInspection,
    HostWandVisServices wandVis,
    HostAccessoryServices accessories,
    HostTaintBlockServices taintBlocks,
    HostEnchantmentEffectServices enchantmentEffects,
    HostScribingServices scribing,
    HostWarpServices warp,
    HostItemPropertyServices itemProperties,
    HostItemFactoryServices itemFactories,
    HostBootstrapServices bootstrap
) {
    public HostServices {
        Objects.requireNonNull(aspects, "aspects");
        Objects.requireNonNull(entityAspects, "entityAspects");
        Objects.requireNonNull(entityClassification, "entityClassification");
        Objects.requireNonNull(morphTools, "morphTools");
        Objects.requireNonNull(blockEntities, "blockEntities");
        Objects.requireNonNull(wandInspection, "wandInspection");
        Objects.requireNonNull(wandVis, "wandVis");
        Objects.requireNonNull(accessories, "accessories");
        Objects.requireNonNull(taintBlocks, "taintBlocks");
        Objects.requireNonNull(enchantmentEffects, "enchantmentEffects");
        Objects.requireNonNull(scribing, "scribing");
        Objects.requireNonNull(warp, "warp");
        Objects.requireNonNull(itemProperties, "itemProperties");
        Objects.requireNonNull(itemFactories, "itemFactories");
        Objects.requireNonNull(bootstrap, "bootstrap");
    }
}
