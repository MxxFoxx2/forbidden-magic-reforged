package dev.visfox.forbiddenreforged.test;

import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.List;
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
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.FmBlocks;
import dev.visfox.forbiddenreforged.FmConfig;
import dev.visfox.forbiddenreforged.FmDrops;
import dev.visfox.forbiddenreforged.FmItems;
import dev.visfox.forbiddenreforged.FmRecipes;
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
        "ring_food_recipe_matches_original"
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
        helper.assertTrue(tags != null && tags.getAmount(FmAspects.NETHER) >= 1, "netherrack yields infernus");
        helper.assertTrue(ResearchCategories.getResearch("NETHERSHARDS") != null, "shard research exists");
        ResearchManager.grantAutoUnlock(player);
        helper.assertTrue(ResearchManager.isComplete(player, "NETHERSHARDS"), "the auto-unlock entry is known after a join");
        ItemStack wrathShard = new ItemStack(FmItems.DEADLY_SHARDS.get("wrath").get());
        AspectList shardTags = AspectRegistry.active().getObjectTags(wrathShard.getItem());
        helper.assertTrue(shardTags != null && shardTags.getAmount(FmAspects.WRATH) >= 2, "the wrath shard carries ira");
        helper.assertTrue(FmDrops.rollWrath(2, 2), "the wrath drop rule fires at the original boundary");
        InfusionRecipe skullAxe = (InfusionRecipe) TcRecipes.get("SKULLAXE");
        helper.assertTrue(skullAxe != null, "skull axe recipe registered");
        helper.assertValueEqual(skullAxe.aspects().getAmount(FmAspects.WRATH), 8, "skull axe costs eight ira");
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
        helper.assertValueEqual(wrath.aspects().getAmount(FmAspects.WRATH), 16, "wrath aspect cost");
        helper.succeed();
    }

    private static void morphRecipesMatchTheOriginal(GameTestHelper helper) {
        for (String key : new String[]{"MORPHPICK", "MORPHSWORD", "MORPHSHOVEL", "MORPHAXE"}) {
            InfusionRecipe recipe = (InfusionRecipe) TcRecipes.get(key);
            helper.assertTrue(recipe != null, key + " recipe must exist");
            helper.assertValueEqual(recipe.instability(), 6, key + " instability");
            helper.assertValueEqual(recipe.aspects().getAmount(FmAspects.ENVY), 16, key + " envy cost");
        }
        for (String key : new String[]{"CLUSTER", "IMPACT", "VOIDTOUCHED"}) {
            helper.assertTrue(TcRecipes.get(key) instanceof InfusionEnchantmentRecipe, key + " must be an enchantment recipe");
        }
        InfusionEnchantmentRecipe voidtouched = (InfusionEnchantmentRecipe) TcRecipes.get("VOIDTOUCHED");
        helper.assertValueEqual(voidtouched.aspects().getAmount(FmAspects.ENVY), 24, "voidtouched envy cost");
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
        helper.assertValueEqual(infernalRod.aspects().getAmount(FmAspects.NETHER), 32, "infernal rod nether cost");
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
        helper.assertTrue(recipe.grid().get(4).test(new ItemStack(FmItems.DEADLY_SHARDS.get("taint").get())), "centre slot takes a taint shard");
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

}
