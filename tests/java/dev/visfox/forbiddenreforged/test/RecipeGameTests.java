package dev.visfox.forbiddenreforged.test;

import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.FmBlocks;
import dev.visfox.forbiddenreforged.FmConfig;
import dev.visfox.forbiddenreforged.FmDrops;
import dev.visfox.forbiddenreforged.FmItems;
import dev.visfox.forbiddenreforged.FmRecipes;
import dev.visfox.forbiddenreforged.FmSpawnerMobs;
import dev.visfox.forbiddenreforged.core.research.ForbiddenMagicResearchCatalog;
import dev.visfox.forbiddenreforged.core.research.ResearchDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4ResearchAdapter;
import dev.visfox.forbiddenreforged.core.warp.WarpKind;
import dev.visfox.forbiddenreforged.core.warp.WarpChangeRequested;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4WarpAdapter;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.model.HostContent;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import dev.visfox.forbiddenreforged.integration.api.HostProfile;
import dev.visfox.forbiddenreforged.core.recipe.ForbiddenMagicRecipeCatalog;
import dev.visfox.forbiddenreforged.core.recipe.RecipeDefinition;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4ContentAdapter;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4RecipeAdapter;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4AspectAdapter;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectRegistry;
import thaumcraft.crafting.CraftingRecipeRef;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.InfusionEnchantmentRecipe;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.ShapedArcaneRecipe;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchManager;
import thaumcraft.registry.ModItems;
import thaumcraft.research.Warp;
import thaumcraft.research.WarpType;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class RecipeGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
        DeferredRegister.create(Registries.TEST_FUNCTION, ForbiddenMagic.MODID);

    private static final String[] TESTS = {
        "every_ported_recipe_resolves_its_inputs",
        "chain_from_netherrack_to_wrath_shard",
        "enchantment_recipes_cover_five",
        "morph_recipes_match_the_original",
        "materials_have_crafting_recipes",
        "plant_research_and_recipes",
        "wand_rod_recipes_registered",
        "taintstone_research_and_arcane_recipe",
        "ore_dictionary_entries_became_tags",
        "emerald_transmutation_matches_original",
        "ring_food_recipe_matches_original",
        "subcollar_matches_original",
        "warp_values_match_original",
        "research_layout_matches_original",
        "common_recipe_catalog_matches_tc4_registration",
        "common_research_catalog_matches_tc4_registration",
        "research_catalog_preserves_flags_pages_and_progression",
        "tc4_warp_adapter_preserves_semantic_types_and_item_costs",
        "host_content_identifiers_are_resolved_by_the_tc4_adapter",
        "host_adapter_registry_exposes_tc4_profile"
    };

    static {
        FUNCTIONS.register(TESTS[0], () -> RecipeGameTests::everyPortedRecipeResolvesItsInputs);
        FUNCTIONS.register(TESTS[1], () -> RecipeGameTests::chainFromNetherrackToWrathShard);
        FUNCTIONS.register(TESTS[2], () -> RecipeGameTests::enchantmentRecipesCoverFive);
        FUNCTIONS.register(TESTS[3], () -> RecipeGameTests::morphRecipesMatchTheOriginal);
        FUNCTIONS.register(TESTS[4], () -> RecipeGameTests::materialsHaveCraftingRecipes);
        FUNCTIONS.register(TESTS[5], () -> RecipeGameTests::plantResearchAndRecipes);
        FUNCTIONS.register(TESTS[6], () -> RecipeGameTests::wandRodRecipesRegistered);
        FUNCTIONS.register(TESTS[7], () -> RecipeGameTests::taintstoneResearchAndArcaneRecipe);
        FUNCTIONS.register(TESTS[8], () -> RecipeGameTests::oreDictionaryEntriesBecameTags);
        FUNCTIONS.register(TESTS[9], () -> RecipeGameTests::emeraldTransmutationMatchesOriginal);
        FUNCTIONS.register(TESTS[10], () -> RecipeGameTests::ringFoodRecipeMatchesOriginal);
        FUNCTIONS.register(TESTS[11], () -> RecipeGameTests::subcollarMatchesOriginal);
        FUNCTIONS.register(TESTS[12], () -> RecipeGameTests::warpValuesMatchOriginal);
        FUNCTIONS.register(TESTS[13], () -> RecipeGameTests::researchLayoutMatchesOriginal);
        FUNCTIONS.register(TESTS[14], () -> RecipeGameTests::commonRecipeCatalogMatchesTc4Registration);
        FUNCTIONS.register(TESTS[15], () -> RecipeGameTests::commonResearchCatalogMatchesTc4Registration);
        FUNCTIONS.register(TESTS[16], () -> RecipeGameTests::researchCatalogPreservesFlagsPagesAndProgression);
        FUNCTIONS.register(TESTS[17], () -> RecipeGameTests::tc4WarpAdapterPreservesSemanticTypesAndItemCosts);
        FUNCTIONS.register(TESTS[18], () -> RecipeGameTests::hostContentIdentifiersAreResolvedByTc4Adapter);
        FUNCTIONS.register(TESTS[19], () -> RecipeGameTests::hostAdapterRegistryExposesTc4Profile);
    }

    private RecipeGameTests() {}

    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
            Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, "fm_recipes"), new TestEnvironmentDefinition.AllOf());
        for (String name : TESTS) {
            ResourceKey<Consumer<GameTestHelper>> function =
                ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, name));
            event.registerTest(
                Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, name),
                new FunctionGameTestInstance(function, new TestData<>(environment, Identifier.parse("thaumcraft:test/platform"), 100, 0, true))
            );
        }
    }

    private static void hostContentIdentifiersAreResolvedByTc4Adapter(GameTestHelper helper) {
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

    private static void hostAdapterRegistryExposesTc4Profile(GameTestHelper helper) {
        HostProfile profile = HostAdapterRegistry.require().profile();
        helper.assertValueEqual(profile.id(), ContentId.parse("host:tc4-reforged"), "selected host profile id");
        helper.assertValueEqual(profile.hostVersion(), "1.1.2", "selected host dependency version");
        helper.assertValueEqual(profile.minecraftVersion(), "26.3", "selected Minecraft version");
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.ASPECT_REGISTRATION).level() == HostProfile.SupportLevel.ADAPTED,
            "aspect registration is exposed through the adapter"
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
            profile.supportOf(HostProfile.Feature.WAND_RUNTIME).level() == HostProfile.SupportLevel.ADAPTED,
            "wand runtime behavior is declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.TAINT_RUNTIME).level() == HostProfile.SupportLevel.ADAPTED,
            "taint runtime behavior is declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.SCRIBING_RUNTIME).level() == HostProfile.SupportLevel.ADAPTED,
            "Scribing Well runtime behavior is declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.ITEM_REGISTRATION).level() == HostProfile.SupportLevel.ADAPTED,
            "host-specific item registration is declared by the selected adapter"
        );
        helper.assertTrue(
            profile.supportOf(HostProfile.Feature.BLOCK_ENTITY_IMPLEMENTATION).level() == HostProfile.SupportLevel.ADAPTED,
            "host block entity implementation is declared by the selected adapter"
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
            profile.supportOf(HostProfile.Feature.WAND_TRIGGERS).level() == HostProfile.SupportLevel.ADAPTED,
            "host wand triggers are declared by the selected adapter"
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
        helper.assertTrue(
            HostAdapterRegistry.requireWands() != null,
            "the selected host exposes wand services"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireTaint() != null,
            "the selected host exposes taint services"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireScribing() != null,
            "the selected host exposes Scribing Well services"
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
            HostAdapterRegistry.requireItems() != null,
            "the selected host exposes its item factory boundary"
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
            HostAdapterRegistry.requireTaint().taintShovelMiningSpeed() > 0.0F,
            "host taint tool properties are retrieved through runtime services"
        );
        helper.assertFalse(
            HostAdapterRegistry.requireMorphTools().isAreaHarvesting(),
            "the host area-harvest state is queried through runtime services"
        );
        helper.assertTrue(
            HostAdapterRegistry.requireItems().primordialPearlItem() != Items.AIR,
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

    private static void commonRecipeCatalogMatchesTc4Registration(GameTestHelper helper) {
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

    private static void commonResearchCatalogMatchesTc4Registration(GameTestHelper helper) {
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

    private static void researchCatalogPreservesFlagsPagesAndProgression(GameTestHelper helper) {
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

    private static void tc4WarpAdapterPreservesSemanticTypesAndItemCosts(GameTestHelper helper) {
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
        ServerPlayer player = mockPlayer(helper);
        int stickyBefore = Warp.get(player, WarpType.STICKY);
        WarpChangeRequested.request(player, 1, WarpKind.STICKY);
        helper.assertValueEqual(Warp.get(player, WarpType.STICKY), stickyBefore + 1,
            "common warp request must reach the TC4 runtime adapter");
        helper.succeed();
    }

    private static ServerPlayer mockPlayer(GameTestHelper helper) {
        ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(player.getGameProfile(), false);
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
            @Override
            public void send(Packet<?> packet) {
            }
        };
        player.connection.markClientLoaded();
        return player;
    }

    private static void everyPortedRecipeResolvesItsInputs(GameTestHelper helper) {
        List<String> missing = new ArrayList<>();
        for (TcRecipe recipe : TcRecipes.all()) {
            if (!FmRecipes.OWN_KEYS.contains(recipe.key())) {
                continue;
            }
            if (recipe instanceof InfusionRecipe infusion) {
                checkIngredient(infusion.central(), missing);
                infusion.components().forEach(ingredient -> checkIngredient(ingredient, missing));
                checkStack(infusion.result(), missing);
            }
        }
        helper.assertTrue(missing.isEmpty(), "unresolved recipe references: " + missing);
        helper.succeed();
    }

    private static void checkIngredient(TcIngredient ingredient, List<String> missing) {
        if (!ingredient.exists()) {
            missing.add(ingredient.toString());
        }
    }

    private static void checkStack(TcStack stack, List<String> missing) {
        if (!stack.exists()) {
            missing.add(stack.item().toString());
        }
    }

    private static void chainFromNetherrackToWrathShard(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        AspectList tags = AspectRegistry.active().getObjectTags(Items.NETHERRACK);
        helper.assertTrue(tags != null && tags.getAmount(Tc4AspectAdapter.resolveAspect(FmAspects.NETHER)) >= 1, "netherrack yields infernus");
        helper.assertTrue(ResearchCategories.getResearch("NETHERSHARDS") != null, "shard research exists");
        ResearchManager.grantAutoUnlock(player);
        helper.assertTrue(ResearchManager.isComplete(player, "NETHERSHARDS"), "the auto-unlock entry is known after a join");
        ItemStack wrathShard = new ItemStack(FmItems.DEADLY_SHARDS.get("wrath").get());
        AspectList shardTags = AspectRegistry.active().getObjectTags(wrathShard.getItem());
        helper.assertTrue(shardTags != null && shardTags.getAmount(Tc4AspectAdapter.resolveAspect(FmAspects.WRATH)) >= 2, "the wrath shard carries ira");
        helper.assertTrue(FmDrops.rollWrath(2, 2), "the wrath drop rule fires at the original boundary");
        InfusionRecipe skullAxe = (InfusionRecipe) TcRecipes.get("SKULLAXE");
        helper.assertTrue(skullAxe != null, "skull axe recipe registered");
        helper.assertValueEqual(skullAxe.aspects().getAmount(Tc4AspectAdapter.resolveAspect(FmAspects.WRATH)), 8, "skull axe costs eight ira");
        helper.succeed();
    }

    private static void enchantmentRecipesCoverFive(GameTestHelper helper) {
        for (String key : new String[]{"WRATH", "GREEDY", "CONSUMING", "EDUCATIONAL", "CORRUPTING"}) {
            TcRecipe recipe = TcRecipes.get(key);
            helper.assertTrue(recipe instanceof InfusionEnchantmentRecipe, key + " must be an infusion enchantment recipe");
            InfusionEnchantmentRecipe infusion = (InfusionEnchantmentRecipe) recipe;
            helper.assertValueEqual(infusion.enchantment(), Identifier.parse("forbiddenmagic:" + key.toLowerCase(java.util.Locale.ROOT)), key + " enchantment id");
        }
        InfusionEnchantmentRecipe wrath = (InfusionEnchantmentRecipe) TcRecipes.get("WRATH");
        helper.assertValueEqual(wrath.instability(), 8, "wrath instability");
        helper.assertValueEqual(wrath.aspects().getAmount(Tc4AspectAdapter.resolveAspect(FmAspects.WRATH)), 16, "wrath aspect cost");
        helper.succeed();
    }

    private static void morphRecipesMatchTheOriginal(GameTestHelper helper) {
        for (String key : new String[]{"MORPHPICK", "MORPHSWORD", "MORPHSHOVEL", "MORPHAXE"}) {
            InfusionRecipe recipe = (InfusionRecipe) TcRecipes.get(key);
            helper.assertTrue(recipe != null, key + " recipe must exist");
            helper.assertValueEqual(recipe.instability(), 6, key + " instability");
            helper.assertValueEqual(recipe.aspects().getAmount(Tc4AspectAdapter.resolveAspect(FmAspects.ENVY)), 16, key + " envy cost");
        }
        for (String key : new String[]{"CLUSTER", "IMPACT", "VOIDTOUCHED"}) {
            helper.assertTrue(TcRecipes.get(key) instanceof InfusionEnchantmentRecipe, key + " must be an enchantment recipe");
        }
        InfusionEnchantmentRecipe voidtouched = (InfusionEnchantmentRecipe) TcRecipes.get("VOIDTOUCHED");
        helper.assertValueEqual(voidtouched.aspects().getAmount(Tc4AspectAdapter.resolveAspect(FmAspects.ENVY)), 24, "voidtouched envy cost");
        helper.succeed();
    }

    private static void materialsHaveCraftingRecipes(GameTestHelper helper) {
        var manager = helper.getLevel().getServer().getRecipeManager();
        for (String name : new String[]{"taint_planks", "taint_stone_brick", "star_block", "star_from_block"}) {
            var key = ResourceKey.create(Registries.RECIPE, Identifier.parse("forbiddenmagic:" + name));
            helper.assertTrue(manager.byKey(key).isPresent(), name + " recipe must load");
        }
        helper.succeed();
    }

    private static void plantResearchAndRecipes(GameTestHelper helper) {
        ResearchItem tree = ResearchCategories.getResearch("TAINTTREE");
        helper.assertTrue(tree != null, "taint tree research must exist");
        helper.assertValueEqual(tree.pages().size(), 2, "taint tree has text and crucible pages");
        ResearchItem flower = ResearchCategories.getResearch("BLACKFLOWER");
        helper.assertTrue(flower != null, "black flower research must exist");
        helper.assertValueEqual(flower.pages().size(), 3, "black flower has text, crucible, and ink crafting pages");
        CrucibleRecipe taintTree = (CrucibleRecipe) TcRecipes.get("TAINTTREE");
        helper.assertTrue(taintTree != null, "taint tree recipe exists");
        helper.assertValueEqual(taintTree.aspects().getAmount(Aspect.TAINT), 10, "taint tree needs ten taint");
        CrucibleRecipe blackFlower = (CrucibleRecipe) TcRecipes.get("BLACKFLOWER");
        helper.assertTrue(blackFlower != null, "black flower recipe exists");
        helper.assertValueEqual(blackFlower.aspects().getAmount(Aspect.DARKNESS), 8, "black flower needs eight darkness");

        TcRecipe inkRef = TcRecipes.get("BlackInk");
        helper.assertTrue(inkRef instanceof CraftingRecipeRef, "black flower ink recipe is exposed in research");
        CraftingRecipeRef ink = (CraftingRecipeRef) inkRef;
        helper.assertValueEqual(ink.recipe(), ForbiddenMagic.id("ink_from_rose"), "ink research page points to the rose crafting recipe");
        ItemStack inkResult = ink.result().create();
        helper.assertTrue(inkResult.is(FmItems.BLACK_DYE.get()), "rose petals yield the legacy black dye resource");
        helper.assertValueEqual(inkResult.getCount(), 2, "black ink recipe yields two dye items");
        var inkRecipeKey = ResourceKey.create(Registries.RECIPE, ForbiddenMagic.id("ink_from_rose"));
        helper.assertTrue(helper.getLevel().getServer().getRecipeManager().byKey(inkRecipeKey).isPresent(),
            "the linked black ink crafting recipe loads");

        helper.succeed();
    }

    private static void wandRodRecipesRegistered(GameTestHelper helper) {
        InfusionRecipe taintedRod = (InfusionRecipe) TcRecipes.get("ROD_TAINTED");
        helper.assertTrue(taintedRod != null, "tainted rod recipe must exist");
        helper.assertValueEqual(taintedRod.instability(), 5, "tainted rod instability");
        helper.assertValueEqual(taintedRod.aspects().getAmount(Aspect.TAINT), 24, "tainted rod taint cost");
        InfusionRecipe infernalRod = (InfusionRecipe) TcRecipes.get("ROD_INFERNAL");
        helper.assertTrue(infernalRod != null, "infernal rod recipe must exist");
        helper.assertValueEqual(infernalRod.instability(), 5, "infernal rod instability");
        helper.assertValueEqual(infernalRod.aspects().getAmount(Tc4AspectAdapter.resolveAspect(FmAspects.NETHER)), 32, "infernal rod nether cost");
        helper.succeed();
    }

    private static void taintstoneResearchAndArcaneRecipe(GameTestHelper helper) {
        ResearchItem research = ResearchCategories.getResearch("TAINTSTONE");
        helper.assertTrue(research != null, "taint stone research must exist");
        helper.assertTrue(research.isConcealed(), "taint stone research is concealed");
        helper.assertValueEqual(research.pages().size(), 3, "taint stone has text, arcane and brick pages");
        helper.assertValueEqual(research.parents(), List.of("THAUMIUM", "INFUSION", "ETHEREALBLOOM"), "taint stone parents");

        ShapedArcaneRecipe recipe = (ShapedArcaneRecipe) TcRecipes.get("TaintStone");
        helper.assertTrue(recipe != null, "taint stone arcane recipe exists");
        helper.assertValueEqual(recipe.width(), 3, "taint stone grid width");
        helper.assertValueEqual(recipe.height(), 3, "taint stone grid height");
        ItemStack result = recipe.result().create();
        helper.assertTrue(result.is(FmBlocks.TAINT_STONE_SMOOTH.get().asItem()), "arcane recipe yields tainted stone");
        helper.assertValueEqual(result.getCount(), 9, "arcane recipe yields nine blocks");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.ENTROPY), 2, "taint stone entropy cost");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.ORDER), 1, "taint stone order cost");
        helper.assertTrue(recipe.grid().get(4).test(new ItemStack(FmItems.DEADLY_SHARDS.get("taint").get())), "centre slot takes a Forbidden Magic taint shard");
        helper.assertTrue(recipe.grid().get(4).test(new ItemStack(ModItems.TAINTED_GOO.get())), "centre slot also takes the modern equivalent of original Thaumcraft ItemResource meta 11 (Taint Slime/Tainted Goo)");
        TagKey<Item> taintIngredients = TagKey.create(Registries.ITEM, Identifier.parse("forbiddenmagic:taint_ingredients"));
        helper.assertTrue(new ItemStack(FmItems.DEADLY_SHARDS.get("taint").get()).is(taintIngredients), "Forbidden Magic taint shard is tagged as a taint ingredient");
        helper.assertTrue(new ItemStack(ModItems.TAINTED_GOO.get()).is(taintIngredients), "Thaumcraft tainted goo is tagged as a taint ingredient");
        for (int slot : new int[]{0, 1, 2, 3, 5, 6, 7, 8}) {
            helper.assertTrue(recipe.grid().get(slot).test(new ItemStack(Items.STONE)), "slot " + slot + " takes plain stone");
        }

        TcRecipe brick = TcRecipes.get("TaintBrick");
        helper.assertTrue(brick instanceof CraftingRecipeRef ref, "brick recipe is registered as a reference");
        helper.assertValueEqual(((CraftingRecipeRef) brick).recipe(), ForbiddenMagic.id("taint_stone_brick"), "brick recipe id");
        helper.succeed();
    }

    private static void emeraldTransmutationMatchesOriginal(GameTestHelper helper) {
        if (!FmConfig.emeraldTrans()) {
            helper.assertTrue(TcRecipes.get("TRANSEMERALD") == null, "disabled emerald transmutation has no recipe");
            helper.succeed();
            return;
        }

        CrucibleRecipe recipe = (CrucibleRecipe) TcRecipes.get("TRANSEMERALD");
        helper.assertTrue(recipe != null, "emerald transmutation crucible recipe exists");
        helper.assertValueEqual(recipe.result().count(), 4, "transmutation produces four emerald nuggets");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.CRYSTAL), 2, "transmutation costs two crystal");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.GREED), 2, "transmutation costs two greed");
        helper.assertTrue(recipe.catalyst().test(new ItemStack(FmItems.EMERALD_FRAGMENT.get())),
            "emerald fragment is accepted as the nuggetEmerald catalyst");
        TagKey<Item> emeraldNuggets = TagKey.create(Registries.ITEM, Identifier.parse("c:nuggets/emerald"));
        helper.assertTrue(new ItemStack(FmItems.EMERALD_FRAGMENT.get()).is(emeraldNuggets),
            "emerald fragment carries the common emerald nugget tag");

        ResearchItem research = ResearchCategories.getResearch("TRANSEMERALD");
        helper.assertTrue(research != null, "transmutation research exists when enabled");
        helper.assertValueEqual(research.pages().size(), 2, "transmutation research has text and crucible pages");
        helper.assertTrue(research.isSecondary() && research.isConcealed(), "transmutation research retains secondary/concealed status");
        helper.succeed();
    }

    private static void oreDictionaryEntriesBecameTags(GameTestHelper helper) {
        TagKey<Item> netherStars = TagKey.create(Registries.ITEM, Identifier.parse("c:nether_stars"));
        helper.assertTrue(new ItemStack(FmBlocks.STAR_BLOCK.get().asItem()).is(netherStars), "the star block joins c:nether_stars like the blockNetherStar ore entry");

        TagKey<Item> taintShards = TagKey.create(Registries.ITEM, Identifier.parse("forbiddenmagic:taint_shards"));
        helper.assertTrue(new ItemStack(FmItems.DEADLY_SHARDS.get("taint").get()).is(taintShards), "the taint shard joins forbiddenmagic:taint_shards like the resourceTaint ore entry");

        ShapedArcaneRecipe recipe = (ShapedArcaneRecipe) TcRecipes.get("TaintStone");
        helper.assertTrue(recipe != null, "taint stone recipe exists");
        helper.assertTrue(recipe.grid().get(4).test(new ItemStack(FmItems.DEADLY_SHARDS.get("taint").get())), "the arcane centre accepts the taint shard tag");
        helper.succeed();
    }
    
    private static void ringFoodRecipeMatchesOriginal(GameTestHelper helper) {
        TcRecipe base = TcRecipes.get("RINGFOOD");
        helper.assertTrue(base instanceof ShapedArcaneRecipe, "Ring of Nutrition is an arcane crafting recipe");
        ShapedArcaneRecipe recipe = (ShapedArcaneRecipe) base;
        helper.assertValueEqual(recipe.width(), 3, "RingFood recipe width");
        helper.assertValueEqual(recipe.height(), 3, "RingFood recipe height");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.ENTROPY), 10, "RingFood entropy cost");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.WATER), 20, "RingFood water cost");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.EARTH), 20, "RingFood earth cost");
        helper.assertValueEqual(recipe.grid().size(), 9, "RingFood has a 3x3 ingredient grid");
        helper.assertTrue(recipe.grid().get(4).exists(), "mundane ring is the center ingredient");
        helper.succeed();
    }

    private static void researchLayoutMatchesOriginal(GameTestHelper helper) {
        assertResearchLayout(helper, "MORPHTOOLS", -9, -3, 3);
        assertResearchLayout(helper, "ROD_tainted", -8, 3, 3);
        if (ModList.get().isLoaded("arsmagica2")) {
            assertResearchLayout(helper, "ROD_witchwood", -3, 1, 3);
            assertResearchLayout(helper, "ROD_witchwood_staff", -3, -1, 2);
        }
        assertResearchLayout(helper, "TAINTSHOVEL", -8, 1, 2);
        assertResearchLayout(helper, "VOIDTOUCHED", -8, -1, 3);
        assertResearchLayout(helper, "WRATH", -6, -3, 3);
        assertResearchLayout(helper, "WRATHCAGE", -6, -9, 3);

        ResearchItem taintedRod = ResearchCategories.getResearch("ROD_tainted");
        helper.assertTrue(taintedRod.isConcealed(), "tainted rod research is concealed");
        helper.assertFalse(taintedRod.isHidden(), "tainted rod research is not hidden");
        helper.assertValueEqual(taintedRod.tags().getAmount(Aspect.MAGIC), 4, "tainted rod MAGIC cost");
        helper.assertValueEqual(taintedRod.tags().getAmount(Aspect.TAINT), 5, "tainted rod TAINT cost");
        helper.assertValueEqual(taintedRod.tags().getAmount(Aspect.TOOL), 2, "tainted rod TOOL cost");
        helper.assertValueEqual(taintedRod.parents(), List.of("ROD_silverwood", "TAINTSHOVEL", "INFUSION"), "tainted rod parents");

        ResearchItem taintShovel = ResearchCategories.getResearch("TAINTSHOVEL");
        helper.assertValueEqual(taintShovel.tags().getAmount(Aspect.CRYSTAL), 3, "taint shovel CRYSTAL cost");
        helper.assertValueEqual(taintShovel.tags().getAmount(Aspect.TAINT), 2, "taint shovel TAINT cost");
        helper.assertValueEqual(taintShovel.tags().getAmount(Aspect.TOOL), 1, "taint shovel TOOL cost");
        helper.assertValueEqual(taintShovel.parents(), List.of("THAUMIUM", "INFUSION", "ETHEREALBLOOM"), "taint shovel parents");

        ResearchItem wrathCage = ResearchCategories.getResearch("WRATHCAGE");
        helper.assertValueEqual(wrathCage.parents(), List.of("FORK"), "Wrath Cage depends on Diabolist Fork");
        ResearchItem profaneRod = ResearchCategories.getResearch("ROD_profane");
        helper.assertTrue(profaneRod.isRound(), "Profane Rod research has a round icon");
        helper.assertTrue(profaneRod.isHidden(), "Profane Rod research remains hidden");
        helper.assertTrue(profaneRod.isStub(), "Profane Rod research remains a stub");
        helper.succeed();
    }

    private static void assertResearchLayout(GameTestHelper helper, String id, int column, int row, int complexity) {
        ResearchItem research = ResearchCategories.getResearch(id);
        helper.assertTrue(research != null, id + " research exists");
        helper.assertValueEqual(research.column(), column, id + " column");
        helper.assertValueEqual(research.row(), row, id + " row");
        helper.assertValueEqual(research.complexity(), complexity, id + " complexity");
    }

    private static void warpValuesMatchOriginal(GameTestHelper helper) {
        helper.assertValueEqual(ResearchCategories.getResearch("ROD_infernal").warp(), 2, "infernal rod research warp");
        helper.assertValueEqual(ResearchCategories.getResearch("TAINTPICK").warp(), 0, "taint pick research has no warp");
        helper.assertValueEqual(ResearchCategories.getResearch("ROD_tainted").warp(), 2, "tainted rod research warp");
        helper.assertValueEqual(ResearchCategories.getResearch("TAINTTREE").warp(), 1, "taint tree research warp");
        if (ModList.get().isLoaded("arsmagica2")) {
            helper.assertValueEqual(ResearchCategories.getResearch("ROD_witchwood").warp(), 2, "witchwood research warp");
        }
        helper.assertValueEqual(Warp.getItemWarp(new ItemStack(FmBlocks.WRATH_CAGE.get())), 5, "wrath cage item warp");
        helper.assertValueEqual(Warp.getItemWarp(new ItemStack(FmBlocks.TAINT_SAPLING.get())), 1, "taint sapling item warp");
        helper.succeed();
    }

    private static void subcollarMatchesOriginal(GameTestHelper helper) {
        if (FmConfig.lustDisabled()) {
            helper.assertTrue(BuiltInRegistries.ITEM.getOptional(ForbiddenMagic.id("sub_collar")).isEmpty(), "no_lust leaves out the collar item");
            helper.assertTrue(TcRecipes.get("SUBCOLLAR") == null, "no_lust leaves out the collar recipe");
            helper.assertTrue(ResearchCategories.getResearch("SUBCOLLAR") == null, "no_lust leaves out the collar research");
            helper.succeed();
            return;
        }
        helper.assertTrue(BuiltInRegistries.ITEM.getOptional(ForbiddenMagic.id("sub_collar")).isPresent(), "lust mode registers the collar item");
        TcRecipe raw = TcRecipes.get("SUBCOLLAR");
        helper.assertTrue(raw instanceof InfusionRecipe, "the collar is an infusion recipe");
        InfusionRecipe recipe = (InfusionRecipe) raw;
        helper.assertValueEqual(recipe.instability(), 1, "collar instability");
        helper.assertValueEqual(recipe.aspects().getAmount(Tc4AspectAdapter.resolveAspect(FmAspects.LUST)), 8, "collar lust cost");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.TRAP), 8, "collar trap cost");
        helper.assertValueEqual(recipe.aspects().getAmount(Tc4AspectAdapter.resolveAspect(FmAspects.NETHER)), 4, "collar nether cost");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.FLESH), 4, "collar flesh cost");
        helper.assertTrue(recipe.central().test(new ItemStack(ModItems.VIS_AMULET.get())), "the centre is an amulet of vis storage");
        helper.assertValueEqual(recipe.components().size(), 5, "three lust shards, lead and a mundane belt");
        long lustShards = recipe.components().stream()
            .filter(ingredient -> ingredient.test(new ItemStack(FmItems.DEADLY_SHARDS.get("lust").get())))
            .count();
        helper.assertValueEqual((int) lustShards, 3, "three lust shards");
        helper.assertTrue(recipe.components().stream().anyMatch(ingredient -> ingredient.test(new ItemStack(Items.LEAD))), "lead is a component");
        helper.assertTrue(recipe.components().stream().anyMatch(ingredient -> ingredient.test(new ItemStack(ModItems.MUNDANE_BELT.get()))), "a mundane belt is a component");

        ResearchItem research = ResearchCategories.getResearch("SUBCOLLAR");
        helper.assertTrue(research != null, "collar research exists");
        helper.assertValueEqual(research.pages().size(), 2, "collar has text and infusion pages");
        helper.assertValueEqual(research.parents(), List.of("NETHERSHARDS", "VISAMULET", "INFUSION"), "collar parents");
        helper.assertTrue(research.isHidden(), "collar research is hidden");
        helper.assertTrue(research.aspectTriggers().contains(Tc4AspectAdapter.resolveAspect(FmAspects.LUST)), "lust triggers the collar research");
        helper.succeed();
    }

}
