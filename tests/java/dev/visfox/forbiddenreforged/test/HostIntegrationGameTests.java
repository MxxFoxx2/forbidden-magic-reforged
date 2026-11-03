package dev.visfox.forbiddenreforged.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.FmBlocks;
import dev.visfox.forbiddenreforged.FmConfig;
import dev.visfox.forbiddenreforged.FmRecipes;
import dev.visfox.forbiddenreforged.FmSpawnerMobs;
import dev.visfox.forbiddenreforged.FmWandContractAccess;
import dev.visfox.forbiddenreforged.core.research.ForbiddenMagicResearchCatalog;
import dev.visfox.forbiddenreforged.core.research.ResearchDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4ResearchAdapter;
import dev.visfox.forbiddenreforged.core.warp.WarpKind;
import dev.visfox.forbiddenreforged.core.warp.WarpAccess;
import dev.visfox.forbiddenreforged.core.wand.WandBehaviors;
import dev.visfox.forbiddenreforged.core.wand.WandPartsAccess;
import dev.visfox.forbiddenreforged.core.wand.WandVisAccess;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4WarpAdapter;
import dev.visfox.forbiddenreforged.core.model.HostContent;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import dev.visfox.forbiddenreforged.integration.api.HostProfile;
import dev.visfox.forbiddenreforged.core.recipe.ForbiddenMagicRecipeCatalog;
import dev.visfox.forbiddenreforged.core.recipe.RecipeDefinition;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4ContentAdapter;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4RecipeAdapter;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4AspectAdapter;
import thaumcraft.crafting.TcRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.Warp;
import thaumcraft.research.WarpType;

final class HostIntegrationGameTests {
    private HostIntegrationGameTests() {
    }

    static void hostContentIdentifiersAreResolvedByTc4Adapter(GameTestHelper helper) {
        helper.assertValueEqual(HostContent.item("thaumium_axe").toString(), "host:item/thaumium_axe",
            "core item references use a host-neutral namespace");
        helper.assertValueEqual(Tc4ContentAdapter.resolveItem(HostContent.item("thaumium_axe")),
            Identifier.parse("thaumcraft:thaumium_axe"),
            "TC4 adapter resolves the semantic thaumium axe key");
        helper.assertValueEqual(Tc4ContentAdapter.resolveTag(HostContent.tag("shards")),
            Identifier.parse("thaumcraft:shards"),
            "TC4 adapter resolves the semantic shards tag");
        helper.assertValueEqual(Tc4ContentAdapter.resolveItem(ContentId.parse("minecraft:diamond")),
            Identifier.parse("minecraft:diamond"),
            "vanilla item identifiers pass through unchanged");
        helper.succeed();
    }

    static void hostAdapterRegistryExposesTc4Profile(GameTestHelper helper) {
        HostProfile profile = HostAdapterRegistry.require().profile();
        assertTc4FeatureSupport(helper, profile);
        helper.assertValueEqual(profile.id(), ContentId.parse("host:tc4-reforged"), "selected host profile id");
        helper.assertValueEqual(profile.hostVersion(), "1.1.2", "selected host dependency version");
        helper.assertValueEqual(profile.minecraftVersion(), "26.3", "selected Minecraft version");
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.ASPECT_REGISTRATION).level() == HostProfile.SupportLevel.ADAPTED,
            "aspect registration is exposed through the adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.ASPECT_RECIPE_DERIVATION).level() == HostProfile.SupportLevel.ADAPTED,
            "custom aspect component recipes are declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.ASPECT_SCAN_TARGETS).level() == HostProfile.SupportLevel.ADAPTED,
            "host aspect scan-target registration is declared by the adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.RESEARCH_STAGES).level() == HostProfile.SupportLevel.PARTIAL,
            "multi-stage research limitations are declared by the adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.RUNTIME_BOOTSTRAP).level() == HostProfile.SupportLevel.ADAPTED,
            "runtime bootstrap is declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.ASPECT_DIAGNOSTICS).level() == HostProfile.SupportLevel.ADAPTED,
            "aspect diagnostics are declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.ASPECT_PRESENTATION).level() == HostProfile.SupportLevel.ADAPTED,
            "host aspect presentation is declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.MORPH_TOOL_RUNTIME).level() == HostProfile.SupportLevel.ADAPTED,
            "morph-tool runtime behavior is declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.WAND_INSPECTION).level() == HostProfile.SupportLevel.ADAPTED,
            "wand and focus inspection is declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.WAND_VIS_STORAGE).level() == HostProfile.SupportLevel.ADAPTED,
            "wand and real Vis operations are declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.ACCESSORY_INTEGRATION).level() == HostProfile.SupportLevel.ADAPTED,
            "host accessory slot access is declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.TAINT_BLOCK_SERVICES).level() == HostProfile.SupportLevel.ADAPTED,
            "taint block and shovel behavior is declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.ENCHANTMENT_EFFECTS).level() == HostProfile.SupportLevel.ADAPTED,
            "host-specific enchantment effects are declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.SCRIBING_RUNTIME).level() == HostProfile.SupportLevel.ADAPTED,
            "Scribing Well runtime behavior is declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.ITEM_PROPERTIES).level() == HostProfile.SupportLevel.ADAPTED,
            "host-specific item-property customization is declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.ITEM_FACTORIES).level() == HostProfile.SupportLevel.ADAPTED,
            "host-backed item factories are declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.BLOCK_ENTITY_IMPLEMENTATION).level() == HostProfile.SupportLevel.ADAPTED,
            "host block entity implementation is declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.ESSENTIA_STORAGE).level() == HostProfile.SupportLevel.ADAPTED,
            "Wrath Cage essentia storage is declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.ESSENTIA_TRANSPORT).level() == HostProfile.SupportLevel.ADAPTED,
            "Wrath Cage essentia transport is declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.ENTITY_ASPECTS).level() == HostProfile.SupportLevel.ADAPTED,
            "the selected host declares its entity aspect catalog"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.ENTITY_CLASSIFICATION).level() == HostProfile.SupportLevel.ADAPTED,
            "host entity classification predicates are declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.WAND_RODS).level() == HostProfile.SupportLevel.ADAPTED,
            "host wand rod registration is declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.WAND_CAPS).level() == HostProfile.SupportLevel.ADAPTED,
            "host wand cap registration is declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.WAND_PARTS).level() == HostProfile.SupportLevel.ADAPTED,
            "host wand part replacement is declared by the selected adapter"
        );
        ItemStack wandPartsProbe = thaumcraft.wand.Wands.create(
            thaumcraft.wand.WandRegistry.CAP_THAUMIUM,
            java.util.Objects.requireNonNull(thaumcraft.wand.WandRegistry.findRod("infernal")),
            false
        );
        new dev.visfox.forbiddenreforged.integration.tc4.Tc4WandPartsAdapter().setRod(
            wandPartsProbe,
            ContentId.parse("forbiddenmagic:profaned")
        );
        helper.assertValueEqual(
            thaumcraft.wand.Wands.getRod(wandPartsProbe).tag(),
            "profaned",
            "the host wand-parts adapter replaces the rod while preserving wand data"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.WAND_TRIGGERS).level() == HostProfile.SupportLevel.ADAPTED,
            "host wand triggers are declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.WARP_POOLS).level() == HostProfile.SupportLevel.ADAPTED,
            "host warp pool integration is declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.STICKY_WARP).level() == HostProfile.SupportLevel.ADAPTED,
            "sticky warp semantics are declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.TAINT_ORIGINS).level() == HostProfile.SupportLevel.UNSUPPORTED,
            "unimplemented host taint-origin integration remains explicitly unsupported"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.OPTIONAL_ENCHANTMENT_INTEGRATION).level() == HostProfile.SupportLevel.PARTIAL,
            "optional enchantment integration declares its conditional availability"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireBootstrap() != null,
            "the selected host exposes its bootstrap and creative-tab services"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireAspects() != null,
            "the selected host exposes aspect presentation services"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireMorphTools() != null,
            "the selected host exposes morph-tool services"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireBlockEntities() != null,
            "the selected host exposes block-entity services"
        );
        var wrathCageBridge = HostAdapterRegistry.requireBlockEntities().createWrathCageBlockEntity(
            net.minecraft.core.BlockPos.ZERO,
            FmBlocks.WRATH_CAGE.get().defaultBlockState()
        );
        helper.assertTrue(
            wrathCageBridge instanceof thaumcraft.aspect.AspectSource,
            "Wrath Cage exposes the host essentia storage contract"
        );
        helper.assertTrue(
            wrathCageBridge instanceof thaumcraft.essentia.EssentiaTransport,
            "Wrath Cage exposes the host essentia transport contract"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireWandInspection() != null,
            "the selected host exposes wand and focus inspection"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireWandVis() != null,
            "the selected host exposes Vis operations"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireAccessories() != null,
            "the selected host exposes accessory slot access"
        );
        helper.assertFalse(
            HostAdapterRegistry.requireWandInspection().isWand(new ItemStack(Items.AIR)),
            "the host wand inspection rejects a non-wand item"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireWandInspection().wandRodTag(new ItemStack(Items.AIR)) == null,
            "the host wand rod tag is nullable for a non-wand item"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireTaintBlocks() != null,
            "the selected host exposes taint block and shovel services"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireEnchantmentEffects() != null,
            "the selected host exposes host-specific enchantment effects"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireScribing() != null,
            "the selected host exposes Scribing Well services"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireWarp() != null,
            "the selected host exposes semantic warp mutation services"
        );
        ItemStack contractProbe = new ItemStack(Items.STICK);
        helper.assertTrue(
            FmWandContractAccess.INSTANCE.remaining(contractProbe) == null,
            "a wand without a Profane Contract starts without stored contract progress"
        );
        FmWandContractAccess.INSTANCE.setRemaining(contractProbe, 25000);
        helper.assertValueEqual(
            FmWandContractAccess.INSTANCE.remaining(contractProbe),
            25000,
            "the common Profane Contract component adapter stores remaining progress"
        );
        FmWandContractAccess.INSTANCE.clear(contractProbe);
        helper.assertTrue(
            FmWandContractAccess.INSTANCE.remaining(contractProbe) == null,
            "the common Profane Contract component adapter clears completed progress"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireEntityAspects() != null,
            "the selected host exposes its entity aspect catalog"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireEntityClassification() != null,
            "the selected host exposes entity classification services"
        );
        helper.assertValueEqual(
            FmSpawnerMobs.aspect("thaumcraft:firebat"),
            ContentId.parse("thaumcraft:ignis"),
            "host entity aspects are resolved through the selected adapter"
        );
        helper.assertValueEqual(
            HostAdapterRegistry.requireEntityAspects().aspectCatalog(false).get(ContentId.parse("minecraft:silverfish")),
            ContentId.parse("thaumcraft:bestia"),
            "the host catalog preserves the default silverfish aspect"
        );
        helper.assertValueEqual(
            HostAdapterRegistry.requireEntityAspects().aspectCatalog(true).get(ContentId.parse("minecraft:silverfish")),
            ContentId.parse("thaumcraft:lucrum"),
            "the host catalog preserves the configured silverfish aspect alternative"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireEntityClassification().bossEntityIds().contains(ContentId.parse("thaumcraft:eldritch_warden")),
            "host-specific boss identifiers come from the selected adapter"
        );
        helper.assertTrue(
            FmSpawnerMobs.aspect("not a valid entity id") == null,
            "invalid persisted entity identifiers are ignored safely"
        );
        List<String> mobIds = FmSpawnerMobs.ids();
        helper.assertTrue(
            mobIds.equals(mobIds.stream().sorted().toList()),
            "the host entity catalog produces a stable sorted Mob Crystal list"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireItemProperties() != null,
            "the selected host exposes item-property services"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireItemFactories() != null,
            "the selected host exposes item-factory services"
        );
        helper.assertTrue(
            !HostAdapterRegistry.requireScribing().primalAspectIds().isEmpty(),
            "scripting integrations expose primal aspects as semantic IDs"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireAspects().aspectDescription(FmAspects.NETHER) != null,
            "host aspect descriptions are retrieved through runtime services"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireTaintBlocks().taintShovelMiningSpeed() > 0.0F,
            "host taint tool properties are retrieved through runtime services"
        );
        helper.assertFalse(
            HostAdapterRegistry.requireMorphTools().isAreaHarvesting(),
            "the host area-harvest state is queried through runtime services"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireItemFactories().primordialPearlItem() != Items.AIR,
            "the host provides the Primewell remainder item through item services"
        );
        helper.assertTrue(
            !HostAdapterRegistry.require().aspectDiagnostics().isEmpty(),
            "host diagnostics are retrieved through the shared adapter contract"
        );
        helper.assertTrue(
            HostAdapterRegistry.require().crystalwellUsesShardTag() == Tc4RecipeAdapter.crystalwellUsesShardTag(),
            "Crystalwell ingredient mode is queried through HostAdapter"
        );
        helper.assertValueEqual(FmAspects.NETHER, ContentId.parse("forbiddenmagic:infernus"),
            "custom aspects are exposed as host-neutral content IDs");
        helper.succeed();
    }

    static void profaneWandBehaviorUsesCorePorts(GameTestHelper helper) {
        ServerPlayer player = GameTestPlayers.connected(helper);
        player.tickCount = 20;
        ItemStack wand = new ItemStack(Items.STICK);
        int[] restoredVis = {0};
        int[] warpCalls = {0};
        ContentId[] replacementRod = {null};
        List<WarpKind> warpKinds = new ArrayList<>();
        List<ContentId> primalAspects = List.of(
            ContentId.parse("forbiddenmagic:concept/air"),
            ContentId.parse("forbiddenmagic:concept/earth"),
            ContentId.parse("forbiddenmagic:concept/fire"),
            ContentId.parse("forbiddenmagic:concept/water"),
            ContentId.parse("forbiddenmagic:concept/order"),
            ContentId.parse("forbiddenmagic:concept/entropy")
        );
        WandVisAccess vis = new WandVisAccess() {
            @Override
            public List<ContentId> primalAspects() {
                return primalAspects;
            }

            @Override
            public int maximumVis(ItemStack stack) {
                return 5000;
            }

            @Override
            public int vis(ItemStack stack, ContentId aspect) {
                return 0;
            }

            @Override
            public void addVis(ItemStack stack, ContentId aspect, int amount, boolean apply) {
            }

            @Override
            public void addRealVis(ItemStack stack, ContentId aspect, int amount, boolean apply) {
                restoredVis[0] += amount;
            }

            @Override
            public void fill(ItemStack stack) {
            }
        };
        WandPartsAccess parts = (stack, rod) -> replacementRod[0] = rod;
        WarpAccess warp = (target, amount, kind) -> {
            warpCalls[0] += amount;
            warpKinds.add(kind);
        };

        WandBehaviors.profane(
            wand,
            player,
            vis,
            parts,
            FmWandContractAccess.INSTANCE,
            warp
        );

        helper.assertValueEqual(restoredVis[0], 25000,
            "Profane Wand restores the complete 25000-point contract through Vis access");
        helper.assertValueEqual(replacementRod[0], ContentId.parse("forbiddenmagic:profaned"),
            "Profane Wand replaces its rod through the wand-parts port");
        helper.assertTrue(FmWandContractAccess.INSTANCE.remaining(wand) == null,
            "Profane Wand clears its completed contract through component access");
        helper.assertValueEqual(warpCalls[0], 6,
            "Profane Wand routes five restore rolls and contract completion through warp access");
        helper.assertTrue(warpKinds.stream().allMatch(kind -> kind == WarpKind.STICKY),
            "Profane Wand requests sticky warp for every contract penalty");
        helper.succeed();
    }

    static void commonRecipeCatalogMatchesTc4Registration(GameTestHelper helper) {
        ForbiddenMagicRecipeCatalog.Options options = new ForbiddenMagicRecipeCatalog.Options(
            FmConfig.enchantmentsEnabled(),
            FmConfig.greedyEnchantmentEnabled(),
            FmConfig.emeraldTrans(),
            FmConfig.gluttonyMode(),
            FmConfig.lustDisabled(),
            Tc4RecipeAdapter.crystalwellUsesShardTag()
        );
        List<RecipeDefinition> definitions = ForbiddenMagicRecipeCatalog.definitions(options);
        Set<String> translatedKeys = new HashSet<>();
        for (RecipeDefinition definition : definitions) {
            helper.assertFalse(definition.toString().contains("thaumcraft:"),
                "core recipe definition must not contain concrete TC4 registry IDs: " + definition.id());
            TcRecipe translated = Tc4RecipeAdapter.translate(definition);
            helper.assertTrue(translatedKeys.add(translated.key()),
                "common catalog generated duplicate TC4 recipe key: " + translated.key());
            helper.assertTrue(FmRecipes.OWN_KEYS.contains(translated.key()),
                "translated recipe key is missing from the compatibility key set: " + translated.key());
            TcRecipe registered = TcRecipes.get(translated.key());
            helper.assertTrue(registered != null,
                "common catalog recipe was not registered by TC4 adapter: " + translated.key());
            helper.assertValueEqual(registered.getClass(), translated.getClass(),
                "registered recipe type must match translated common definition: " + translated.key());
        }
        helper.assertValueEqual(translatedKeys.size(), definitions.size(),
            "every common recipe definition must have a unique host recipe key");
        helper.succeed();
    }

    static void commonResearchCatalogMatchesTc4Registration(GameTestHelper helper) {
        ForbiddenMagicResearchCatalog.Options options = new ForbiddenMagicResearchCatalog.Options(
            FmConfig.enchantmentsEnabled(),
            FmConfig.greedyEnchantmentEnabled(),
            FmConfig.lustDisabled(),
            FmConfig.emeraldTrans(),
            ModList.get().isLoaded("arsmagica2"),
            Tc4RecipeAdapter.crystalwellUsesShardTag()
        );
        List<ResearchDefinition> definitions = ForbiddenMagicResearchCatalog.definitions(options);
        Set<String> translatedKeys = new HashSet<>();
        for (ResearchDefinition definition : definitions) {
            ResearchItem translated = Tc4ResearchAdapter.translate(definition);
            helper.assertTrue(translatedKeys.add(translated.key()),
                "common research catalog generated duplicate TC4 key: " + translated.key());
            ResearchItem registered = ResearchCategories.getResearch(translated.key());
            helper.assertTrue(registered != null,
                "common research catalog entry was not registered: " + translated.key());
            if (registered == null) {
                continue;
            }
            helper.assertValueEqual(registered.category(), translated.category(),
                translated.key() + " category");
            helper.assertValueEqual(registered.column(), definition.column(),
                translated.key() + " column");
            helper.assertValueEqual(registered.row(), definition.row(),
                translated.key() + " row");
            helper.assertValueEqual(registered.complexity(),
                Math.max(1, Math.min(3, definition.complexity())),
                translated.key() + " complexity");
            helper.assertValueEqual(registered.parents(), translated.parents(),
                translated.key() + " visible parents");
            helper.assertValueEqual(registered.parentsHidden(), translated.parentsHidden(),
                translated.key() + " hidden parents");
            helper.assertValueEqual(registered.warp(), definition.stages().get(0).warp(),
                translated.key() + " warp");
            helper.assertValueEqual(registered.pages().size(), definition.stages().get(0).pages().size(),
                translated.key() + " page count");
            for (var amount : definition.noteCost().amounts()) {
                helper.assertValueEqual(
                    registered.tags().getAmount(Tc4AspectAdapter.resolveAspect(amount.aspect())),
                    amount.amount(),
                    translated.key() + " note cost " + amount.aspect()
                );
            }
            for (ContentId trigger : definition.aspectTriggers()) {
                helper.assertTrue(registered.aspectTriggers().contains(Tc4AspectAdapter.resolveAspect(trigger)),
                    translated.key() + " aspect trigger " + trigger);
            }
        }
        helper.assertValueEqual(translatedKeys.size(), definitions.size(),
            "every common research definition must have a unique TC4 key");
        helper.succeed();
    }

    static void researchCatalogPreservesFlagsPagesAndProgression(GameTestHelper helper) {
        ForbiddenMagicResearchCatalog.Options options = new ForbiddenMagicResearchCatalog.Options(
            FmConfig.enchantmentsEnabled(),
            FmConfig.greedyEnchantmentEnabled(),
            FmConfig.lustDisabled(),
            FmConfig.emeraldTrans(),
            ModList.get().isLoaded("arsmagica2"),
            Tc4RecipeAdapter.crystalwellUsesShardTag()
        );
        List<ResearchDefinition> definitions = ForbiddenMagicResearchCatalog.definitions(options);
        for (ResearchDefinition definition : definitions) {
            ResearchItem expected = Tc4ResearchAdapter.translate(definition);
            ResearchItem actual = ResearchCategories.getResearch(expected.key());
            helper.assertTrue(actual != null, expected.key() + " is registered");
            if (actual == null) {
                continue;
            }
            helper.assertValueEqual(actual.pages(), expected.pages(), expected.key() + " pages");
            helper.assertValueEqual(actual.isSpecial(), definition.flags().contains(ResearchDefinition.Flag.SPECIAL), expected.key() + " special flag");
            helper.assertValueEqual(actual.isSecondary(), definition.flags().contains(ResearchDefinition.Flag.SECONDARY), expected.key() + " secondary flag");
            helper.assertValueEqual(actual.isRound(), definition.flags().contains(ResearchDefinition.Flag.ROUND_ICON), expected.key() + " round flag");
            helper.assertValueEqual(actual.isStub(), definition.flags().contains(ResearchDefinition.Flag.STUB), expected.key() + " stub flag");
            helper.assertValueEqual(actual.isVirtual(), definition.flags().contains(ResearchDefinition.Flag.VIRTUAL), expected.key() + " virtual flag");
            helper.assertValueEqual(actual.isConcealed(), definition.flags().contains(ResearchDefinition.Flag.CONCEALED), expected.key() + " concealed flag");
            helper.assertValueEqual(actual.isHidden(), definition.flags().contains(ResearchDefinition.Flag.HIDDEN), expected.key() + " hidden flag");
            helper.assertValueEqual(actual.isLost(), definition.flags().contains(ResearchDefinition.Flag.LOST), expected.key() + " lost flag");
            helper.assertValueEqual(actual.isAutoUnlock(), definition.flags().contains(ResearchDefinition.Flag.AUTO_UNLOCK), expected.key() + " auto-unlock flag");
            helper.assertValueEqual(actual.parents(), expected.parents(), expected.key() + " parents");
            helper.assertValueEqual(actual.parentsHidden(), expected.parentsHidden(), expected.key() + " hidden parents");
            helper.assertValueEqual(actual.aspectTriggers(), expected.aspectTriggers(), expected.key() + " aspect triggers");
            helper.assertValueEqual(actual.tags(), expected.tags(), expected.key() + " note costs");
            helper.assertValueEqual(actual.warp(), expected.warp(), expected.key() + " warp");
            helper.assertValueEqual(actual.column(), expected.column(), expected.key() + " column");
            helper.assertValueEqual(actual.row(), expected.row(), expected.key() + " row");
            helper.assertValueEqual(actual.complexity(), expected.complexity(), expected.key() + " complexity");
        }

        ResearchItem taintedRod = ResearchCategories.getResearch("ROD_tainted");
        helper.assertTrue(taintedRod != null, "mixed-case ROD_tainted key is preserved");
        ResearchItem infernalRod = ResearchCategories.getResearch("ROD_infernal");
        helper.assertTrue(infernalRod != null, "mixed-case ROD_infernal key is preserved");
        helper.assertTrue(ResearchCategories.getResearch("ROD_silverwood") != null,
            "mixed-case ROD_silverwood parent resolves");
        helper.succeed();
    }

    static void tc4WarpAdapterPreservesSemanticTypesAndItemCosts(GameTestHelper helper) {
        helper.assertValueEqual(Tc4WarpAdapter.toHostType(WarpKind.PERMANENT), WarpType.PERMANENT,
            "permanent warp must map to TC4 permanent");
        helper.assertValueEqual(Tc4WarpAdapter.toHostType(WarpKind.STICKY), WarpType.STICKY,
            "sticky warp must map to TC4 sticky");
        helper.assertValueEqual(Tc4WarpAdapter.toHostType(WarpKind.TEMPORARY), WarpType.TEMPORARY,
            "temporary warp must map to TC4 temporary");
        helper.assertValueEqual(Warp.getItemWarp(new ItemStack(FmBlocks.WRATH_CAGE.get().asItem())), 5,
            "Wrath Cage item must keep five warp");
        helper.assertValueEqual(Warp.getItemWarp(new ItemStack(FmBlocks.TAINT_SAPLING.get().asItem())), 1,
            "Tainted Sapling item must keep one warp");
        ServerPlayer player = GameTestPlayers.connected(helper);
        int stickyBefore = Warp.get(player, WarpType.STICKY);
        HostAdapterRegistry.requireWarp().changeWarp(player, 0, WarpKind.STICKY);
        helper.assertValueEqual(Warp.get(player, WarpType.STICKY), stickyBefore,
            "zero warp changes must be ignored");
        HostAdapterRegistry.requireWarp().changeWarp(player, 1, WarpKind.STICKY);
        helper.assertValueEqual(Warp.get(player, WarpType.STICKY), stickyBefore + 1,
            "common warp request must reach the TC4 runtime adapter");
        helper.succeed();
    }

    static void assertTc4FeatureSupport(GameTestHelper helper, HostProfile profile) {
        Map<HostProfile.Feature, HostProfile.SupportLevel> expected = Map.ofEntries(
            Map.entry(HostProfile.Feature.ASPECT_REGISTRATION, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ASPECT_RECIPE_DERIVATION, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ASPECT_SCAN_TARGETS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ASPECT_DIAGNOSTICS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ASPECT_PRESENTATION, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.RUNTIME_BOOTSTRAP, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.MORPH_TOOL_RUNTIME, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.WAND_INSPECTION, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ACCESSORY_INTEGRATION, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.TAINT_BLOCK_SERVICES, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ENCHANTMENT_EFFECTS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.SCRIBING_RUNTIME, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ITEM_PROPERTIES, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ITEM_FACTORIES, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.BLOCK_ENTITY_IMPLEMENTATION, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.RESEARCH_ENTRIES, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.RESEARCH_STAGES, HostProfile.SupportLevel.PARTIAL),
            Map.entry(HostProfile.Feature.ARCANE_RECIPES, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.CRAFTING_RECIPE_REFERENCES, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.CRUCIBLE_RECIPES, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.INFUSION_RECIPES, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.INFUSION_ENCHANTMENTS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.WAND_RODS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.WAND_CAPS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.WAND_TRIGGERS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.OPTIONAL_ENCHANTMENT_INTEGRATION, HostProfile.SupportLevel.PARTIAL),
            Map.entry(HostProfile.Feature.WAND_VIS_STORAGE, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.WAND_PARTS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.FOCI_OR_SPELLS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ESSENTIA_STORAGE, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ESSENTIA_TRANSPORT, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.WARP_POOLS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.STICKY_WARP, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.TAINT_ORIGINS, HostProfile.SupportLevel.UNSUPPORTED),
            Map.entry(HostProfile.Feature.ENTITY_ASPECTS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ENTITY_CLASSIFICATION, HostProfile.SupportLevel.ADAPTED)
        );
        helper.assertValueEqual(expected.size(), HostProfile.Feature.values().length,
            "every HostProfile feature has exactly one expected support level");
        for (Map.Entry<HostProfile.Feature, HostProfile.SupportLevel> feature : expected.entrySet()) {
            helper.assertValueEqual(
                profile.supportOf(feature.getKey()).level(),
                feature.getValue(),
                "TC4 host feature support: " + feature.getKey()
            );
        }
    }
}
