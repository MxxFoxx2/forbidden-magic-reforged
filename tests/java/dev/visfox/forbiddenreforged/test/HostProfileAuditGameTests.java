package dev.visfox.forbiddenreforged.test;

import dev.visfox.forbiddenreforged.integration.api.HostAdapter;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import dev.visfox.forbiddenreforged.integration.api.HostProfile;
import dev.visfox.forbiddenreforged.integration.api.HostProfile.Feature;
import dev.visfox.forbiddenreforged.integration.api.HostProfile.FeatureSupport;
import dev.visfox.forbiddenreforged.integration.api.HostProfile.SupportLevel;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder
@PrefixGameTestTemplate(false)
public final class HostProfileAuditGameTests {
    private HostProfileAuditGameTests() {
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void tc4HostProfileMatchesEveryRuntimeCapability(GameTestHelper helper) {
        HostAdapter adapter = HostAdapterRegistry.require();
        HostProfile profile = adapter.profile();

        helper.assertTrue(profile != null, "the selected host exposes its capability profile");
        helper.assertValueEqual(profile.features().size(), Feature.values().length,
            "the profile explicitly declares every HostProfile feature");

        for (Feature feature : Feature.values()) {
            FeatureSupport support = profile.features().get(feature);
            helper.assertTrue(support != null,
                "the capability profile explicitly declares " + feature);
            if (support == null) {
                continue;
            }
            helper.assertTrue(!support.notes().isBlank(),
                "the capability profile explains its support level for " + feature);
            helper.assertValueEqual(support.level(), expectedLevel(feature),
                "the declared support level matches the audited TC4 implementation for " + feature);
        }

        // Typed runtime capabilities. The base HostAdapter intentionally owns
        // catalog registration, scan-target registration, diagnostics and bootstrap.
        helper.assertTrue(HostAdapterRegistry.requireAspects() != null,
            "aspect presentation is exposed through HostAspectServices");
        helper.assertTrue(HostAdapterRegistry.requireMorphTools() != null,
            "morph-tool callbacks are exposed through HostMorphToolServices");
        helper.assertTrue(HostAdapterRegistry.requireBlockEntities() != null,
            "Wrath Cage lifecycle is exposed through HostBlockEntityServices");
        helper.assertTrue(HostAdapterRegistry.requireWandInspection() != null,
            "wand and numeric focus inspection is exposed through HostWandInspectionServices");
        helper.assertTrue(HostAdapterRegistry.requireWandVis() != null,
            "wand and item-backed Vis access is exposed through HostWandVisServices");
        helper.assertTrue(HostAdapterRegistry.requireWandParts() != null,
            "native wand-part mutation is exposed through HostWandPartsServices");
        helper.assertTrue(HostAdapterRegistry.requireFocus() != null,
            "focus upgrade inspection and Vis payment are exposed through HostFocusServices");
        helper.assertTrue(HostAdapterRegistry.requireEssentiaTransport() != null,
            "physical essentia extraction is exposed through HostEssentiaTransportServices");
        helper.assertTrue(HostAdapterRegistry.requireAccessories() != null,
            "accessory slot access is exposed through HostAccessoryServices");
        helper.assertTrue(HostAdapterRegistry.requireTaintBlocks() != null,
            "taint material and purification operations are exposed through HostTaintBlockServices");
        helper.assertTrue(HostAdapterRegistry.requireEnchantmentEffects() != null,
            "host-dependent enchantment effects are exposed through HostEnchantmentEffectServices");
        helper.assertTrue(HostAdapterRegistry.requireScribing() != null,
            "research and scribing operations are exposed through HostScribingServices");
        helper.assertTrue(HostAdapterRegistry.requireWarp() != null,
            "warp mutations are exposed through HostWarpServices");
        helper.assertTrue(HostAdapterRegistry.requireEntityAspects() != null,
            "entity-to-aspect mappings are exposed through HostEntityAspectServices");
        helper.assertTrue(HostAdapterRegistry.requireEntityClassification() != null,
            "host entity classification is exposed through HostEntityClassificationServices");
        helper.assertTrue(HostAdapterRegistry.requireItemProperties() != null,
            "host item properties are exposed through HostItemPropertyServices");
        helper.assertTrue(HostAdapterRegistry.requireItemFactories() != null,
            "host item factories are exposed through HostItemFactoryServices");
        helper.assertTrue(HostAdapterRegistry.requireWandRegistration() != null,
            "wand and trigger registration is exposed through HostWandRegistrationServices");
        helper.assertTrue(HostAdapterRegistry.requireOptionalEnchantments() != null,
            "optional enchantment integration is exposed through HostOptionalEnchantmentServices");
        helper.assertTrue(HostAdapterRegistry.requireCreativeContent() != null,
            "host creative-tab content is exposed through HostCreativeContentServices");
        helper.assertTrue(HostAdapterRegistry.requireVillagerTrades() != null,
            "host villager trades are exposed through HostVillagerTradeServices");

        // Explicit feature-to-capability alignment. Several catalog-level
        // features deliberately use HostAdapter rather than a runtime service.
        assertAdapted(helper, profile, Feature.ASPECT_PRESENTATION, Feature.MORPH_TOOL_RUNTIME,
            Feature.WAND_INSPECTION, Feature.ACCESSORY_INTEGRATION, Feature.TAINT_BLOCK_SERVICES,
            Feature.ENCHANTMENT_EFFECTS, Feature.SCRIBING_RUNTIME, Feature.ITEM_PROPERTIES,
            Feature.ITEM_FACTORIES, Feature.BLOCK_ENTITY_IMPLEMENTATION,
            Feature.WAND_RODS, Feature.WAND_CAPS, Feature.WAND_TRIGGERS,
            Feature.WAND_VIS_STORAGE, Feature.WAND_PARTS, Feature.FOCI_OR_SPELLS,
            Feature.ESSENTIA_STORAGE, Feature.ESSENTIA_TRANSPORT, Feature.WARP_POOLS,
            Feature.STICKY_WARP, Feature.ENTITY_ASPECTS, Feature.ENTITY_CLASSIFICATION,
            Feature.VILLAGER_TRADES, Feature.CREATIVE_CONTENT);
        helper.succeed();
    }

    private static SupportLevel expectedLevel(Feature feature) {
        return switch (feature) {
            case RESEARCH_STAGES, OPTIONAL_ENCHANTMENT_INTEGRATION -> SupportLevel.PARTIAL;
            case TAINT_ORIGINS -> SupportLevel.UNSUPPORTED;
            default -> SupportLevel.ADAPTED;
        };
    }

    private static void assertAdapted(
        GameTestHelper helper,
        HostProfile profile,
        Feature... features
    ) {
        for (Feature feature : features) {
            helper.assertValueEqual(profile.supportOf(feature).level(), SupportLevel.ADAPTED,
                "feature-to-capability mapping remains adapted for " + feature);
        }
    }
}
