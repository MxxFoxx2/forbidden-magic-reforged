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
import dev.visfox.forbiddenreforged.common.FmAspects;
import dev.visfox.forbiddenreforged.common.FmBlocks;
import dev.visfox.forbiddenreforged.common.FmConfig;
import dev.visfox.forbiddenreforged.common.FmRecipes;
import dev.visfox.forbiddenreforged.common.FmSpawnerMobs;
import dev.visfox.forbiddenreforged.common.FmWandContractAccess;
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
        helper.assertValueEqual(profile.id(), ContentId.parse("host:tc4-reforged"), "selected host profile id");
        helper.assertValueEqual(profile.hostVersion(), "1.1.3", "selected host dependency version");
        helper.assertValueEqual(profile.minecraftVersion(), "26.3", "selected Minecraft version");
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.RESEARCH_STAGES).level() == HostProfile.SupportLevel.PARTIAL,
            "multi-stage research limitations are declared by the adapter"
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
            profile.supportOf(HostProfile.Feature.TAINT_ORIGINS).level() == HostProfile.SupportLevel.UNSUPPORTED,
            "unimplemented host taint-origin integration remains explicitly unsupported"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.OPTIONAL_ENCHANTMENT_INTEGRATION).level() == HostProfile.SupportLevel.PARTIAL,
            "optional enchantment integration declares its conditional availability"
        );
        var wrathCageBridge = HostAdapterRegistry.blockEntities().createWrathCageBlockEntity(
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
        helper.assertFalse(
            HostAdapterRegistry.wandInspection().isWand(new ItemStack(Items.AIR)),
            "the host wand inspection rejects a non-wand item"
        );
        helper.assertTrue(
            HostAdapterRegistry.wandInspection().wandRodTag(new ItemStack(Items.AIR)) == null,
            "the host wand rod tag is nullable for a non-wand item"
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
        helper.assertValueEqual(
            FmSpawnerMobs.aspect("thaumcraft:firebat"),
            ContentId.parse("thaumcraft:ignis"),
            "host entity aspects are resolved through the selected adapter"
        );
        helper.assertValueEqual(
            HostAdapterRegistry.entityAspects().aspectCatalog(false).get(ContentId.parse("minecraft:silverfish")),
            ContentId.parse("thaumcraft:bestia"),
            "the host catalog preserves the default silverfish aspect"
        );
        helper.assertValueEqual(
            HostAdapterRegistry.entityAspects().aspectCatalog(true).get(ContentId.parse("minecraft:silverfish")),
            ContentId.parse("thaumcraft:lucrum"),
            "the host catalog preserves the configured silverfish aspect alternative"
        );
        helper.assertTrue(
            HostAdapterRegistry.entityClassification().bossEntityIds().contains(ContentId.parse("thaumcraft:eldritch_warden")),
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
            !HostAdapterRegistry.scribing().primalAspectIds().isEmpty(),
            "scripting integrations expose primal aspects as semantic IDs"
        );
        helper.assertTrue(
            HostAdapterRegistry.aspects().aspectDescription(FmAspects.NETHER) != null,
            "host aspect descriptions are retrieved through runtime services"
        );
        helper.assertTrue(
            HostAdapterRegistry.taintBlocks().taintShovelMiningSpeed() > 0.0F,
            "host taint tool properties are retrieved through runtime services"
        );
        helper.assertFalse(
            HostAdapterRegistry.morphTools().isAreaHarvesting(),
            "the host area-harvest state is queried through runtime services"
        );
        helper.assertTrue(
            HostAdapterRegistry.itemFactories().primordialPearlItem() != Items.AIR,
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
        HostAdapterRegistry.warp().changeWarp(player, 0, WarpKind.STICKY);
        helper.assertValueEqual(Warp.get(player, WarpType.STICKY), stickyBefore,
            "zero warp changes must be ignored");
        HostAdapterRegistry.warp().changeWarp(player, 1, WarpKind.STICKY);
        helper.assertValueEqual(Warp.get(player, WarpType.STICKY), stickyBefore + 1,
            "common warp request must reach the TC4 runtime adapter");
        helper.succeed();
    }


}
