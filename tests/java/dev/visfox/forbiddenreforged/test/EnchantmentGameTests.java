package dev.visfox.forbiddenreforged.test;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.util.RandomSource;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.FmConfig;
import dev.visfox.forbiddenreforged.FmEnchantmentEffects;
import dev.visfox.forbiddenreforged.FmEnchantments;
import dev.visfox.forbiddenreforged.FmItems;
import dev.visfox.forbiddenreforged.FmTinkerer;
import dev.visfox.forbiddenreforged.MorphState;
import dev.visfox.forbiddenreforged.MorphToolItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class EnchantmentGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
        DeferredRegister.create(Registries.TEST_FUNCTION, ForbiddenMagic.MODID);

    private static final String[] TESTS = {
        "enchantments_resolve_with_expected_shape",
        "consuming_destroys_garbage",
        "corrupting_yields_sin_shards",
        "greedy_drops_match_kills",
        "educational_multiplies_experience",
        "wrath_adds_damage",
        "enchantment_research_present",
        "tinkerer_integration_is_optional",
        "morph_tools_are_registered",
        "morph_phase_rotates_sets",
        "impact_covers_a_three_by_three_plane",
        "voidtouched_repairs_and_warps",
        "morph_enchantments_and_exclusivity",
        "cluster_replaces_drops_with_clusters",
        "morph_research_entries_present"
    };

    static {
        FUNCTIONS.register(TESTS[0], () -> EnchantmentGameTests::enchantmentsResolveWithExpectedShape);
        FUNCTIONS.register(TESTS[1], () -> EnchantmentGameTests::consumingDestroysGarbage);
        FUNCTIONS.register(TESTS[2], () -> EnchantmentGameTests::corruptingYieldsSinShards);
        FUNCTIONS.register(TESTS[3], () -> EnchantmentGameTests::greedyDropsMatchKills);
        FUNCTIONS.register(TESTS[4], () -> EnchantmentGameTests::educationalMultipliesExperience);
        FUNCTIONS.register(TESTS[5], () -> EnchantmentGameTests::wrathAddsDamage);
        FUNCTIONS.register(TESTS[6], () -> EnchantmentGameTests::enchantmentResearchPresent);
        FUNCTIONS.register(TESTS[7], () -> EnchantmentGameTests::tinkererIntegrationIsOptional);
        FUNCTIONS.register(TESTS[8], () -> EnchantmentGameTests::morphToolsAreRegistered);
        FUNCTIONS.register(TESTS[9], () -> EnchantmentGameTests::morphPhaseRotatesSets);
        FUNCTIONS.register(TESTS[10], () -> EnchantmentGameTests::impactCoversAThreeByThreePlane);
        FUNCTIONS.register(TESTS[11], () -> EnchantmentGameTests::voidtouchedRepairsAndWarps);
        FUNCTIONS.register(TESTS[12], () -> EnchantmentGameTests::morphEnchantmentsAndExclusivity);
        FUNCTIONS.register(TESTS[13], () -> EnchantmentGameTests::clusterReplacesDropsWithClusters);
        FUNCTIONS.register(TESTS[14], () -> EnchantmentGameTests::morphResearchEntriesPresent);
    }

    private EnchantmentGameTests() {}

    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
            Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, "fm_enchantments"), new TestEnvironmentDefinition.AllOf());
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
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(java.util.UUID.randomUUID(), "test-mock-player"), false);
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation()) {
            @Override
            public GameType gameMode() {
                return GameType.SURVIVAL;
            }
        };
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

    private static void enchantmentsResolveWithExpectedShape(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        helper.assertValueEqual(registry.getOrThrow(FmEnchantments.WRATH).value().getMaxLevel(), 5, "wrath max level");
        helper.assertValueEqual(registry.getOrThrow(FmEnchantments.GREEDY).value().getMaxLevel(), 1, "greedy max level");
        helper.assertValueEqual(registry.getOrThrow(FmEnchantments.EDUCATIONAL).value().getMaxLevel(), 5, "educational max level");
        helper.assertValueEqual(registry.getOrThrow(FmEnchantments.CONSUMING).value().getMaxLevel(), 1, "consuming max level");
        helper.assertValueEqual(registry.getOrThrow(FmEnchantments.CORRUPTING).value().getMaxLevel(), 1, "corrupting max level");
        helper.assertTrue(registry.getOrThrow(FmEnchantments.WRATH).value().isSupportedItem(new ItemStack(Items.DIAMOND_SWORD)), "wrath applies to swords");
        helper.assertTrue(registry.getOrThrow(FmEnchantments.WRATH).value().isSupportedItem(new ItemStack(Items.DIAMOND_AXE)), "wrath applies to axes");
        helper.assertTrue(registry.getOrThrow(FmEnchantments.CONSUMING).value().isSupportedItem(new ItemStack(Items.IRON_SHOVEL)), "consuming applies to shovels");
        helper.assertFalse(registry.getOrThrow(FmEnchantments.CORRUPTING).value().isSupportedItem(new ItemStack(Items.IRON_SHOVEL)), "corrupting is pickaxe only");
        helper.succeed();
    }

    private static void consumingDestroysGarbage(GameTestHelper helper) {
        helper.assertTrue(FmEnchantmentEffects.isGarbage(new ItemStack(Items.DIRT)), "dirt is garbage");
        helper.assertTrue(FmEnchantmentEffects.isGarbage(new ItemStack(Items.COBBLESTONE)), "cobblestone is garbage");
        helper.assertTrue(FmEnchantmentEffects.isGarbage(new ItemStack(Items.SAND)), "sand is garbage");
        helper.assertTrue(FmEnchantmentEffects.isGarbage(new ItemStack(Items.GRAVEL)), "gravel is garbage");
        helper.assertTrue(FmEnchantmentEffects.isGarbage(new ItemStack(Items.NETHERRACK)), "netherrack is garbage");
        helper.assertFalse(FmEnchantmentEffects.isGarbage(new ItemStack(Items.DIAMOND)), "diamond is not garbage");
        helper.succeed();
    }

    private static void corruptingYieldsSinShards(GameTestHelper helper) {
        RandomSource random = RandomSource.create(1234L);
        Set<Item> pool = Set.of(
            FmItems.DEADLY_SHARDS.get("wrath").get(),
            FmItems.DEADLY_SHARDS.get("envy").get(),
            FmItems.DEADLY_SHARDS.get("pride").get(),
            FmItems.DEADLY_SHARDS.get("lust").get(),
            FmItems.DEADLY_SHARDS.get("sloth").get(),
            FmItems.DEADLY_SHARDS.get("greed").get(),
            FmItems.GLUTTONY_SHARD.get()
        );
        for (int roll = 0; roll < 200; roll++) {
            ItemStack shard = FmEnchantmentEffects.sinShard(random);
            helper.assertTrue(pool.contains(shard.getItem()), "sin shard must come from the pool");
            if (FmConfig.lustDisabled()) {
                helper.assertFalse(shard.getItem() == FmItems.DEADLY_SHARDS.get("lust").get(), "no_lust keeps lust out of the pool");
            }
        }
        int hits = 0;
        for (int roll = 0; roll < 300; roll++) {
            if (FmEnchantmentEffects.corruptRoll(random)) {
                hits++;
            }
        }
        helper.assertTrue(hits > 50 && hits < 150, "corruption fires roughly one time in three");
        helper.succeed();
    }

    private static void greedyDropsMatchKills(GameTestHelper helper) {
        helper.assertValueEqual(FmEnchantmentEffects.greedyDrop(true, 99).getItem(), Items.EMERALD, "villager drops an emerald");
        helper.assertValueEqual(FmEnchantmentEffects.greedyDrop(false, 2).getItem(), FmItems.EMERALD_FRAGMENT.get(), "hostile mob drops a fragment on a low roll");
        helper.assertTrue(FmEnchantmentEffects.greedyDrop(false, 3).isEmpty(), "hostile mob drops nothing on a high roll");
        helper.succeed();
    }

    private static void educationalMultipliesExperience(GameTestHelper helper) {
        helper.assertValueEqual(FmEnchantmentEffects.educationalExperience(10, 1), 40, "level 1 adds three times the base xp");
        helper.assertValueEqual(FmEnchantmentEffects.educationalExperience(10, 5), 160, "level 5 adds fifteen times the base xp");
        helper.assertValueEqual(FmEnchantmentEffects.educationalExperience(0, 5), 0, "no base xp stays zero");
        helper.succeed();
    }

    private static void wrathAddsDamage(GameTestHelper helper) {
        helper.assertValueEqual(FmEnchantmentEffects.wrathDamage(1), 1.25F, "level 1 adds 1.25 damage");
        helper.assertValueEqual(FmEnchantmentEffects.wrathDamage(5), 6.25F, "level 5 adds 6.25 damage");
        helper.assertValueEqual(FmEnchantmentEffects.wrathDamage(0), 0.0F, "level 0 adds nothing");
        helper.succeed();
    }

    private static void enchantmentResearchPresent(GameTestHelper helper) {
        ResearchItem wrath = ResearchCategories.getResearch("WRATH");
        helper.assertTrue(wrath != null, "wrath research must exist");
        helper.assertValueEqual(wrath.warp(), 3, "wrath research carries warp 3");
        helper.assertTrue(wrath.parents().contains("NETHERSHARDS"), "wrath descends from nether shards");
        helper.assertTrue(wrath.parents().contains("INFUSIONENCHANTMENT"), "wrath needs infusion enchantment");
        helper.assertValueEqual(wrath.pages().size(), 2, "wrath has text and recipe pages");
        for (String key : new String[]{"GREEDY", "CONSUMING", "EDUCATIONAL", "CORRUPTING"}) {
            helper.assertTrue(ResearchCategories.getResearch(key) != null, key + " research must exist");
        }
        helper.succeed();
    }

    private static void tinkererIntegrationIsOptional(GameTestHelper helper) {
        boolean loaded = ModList.get().isLoaded("thaumictinkerer");
        if (!loaded) {
            helper.assertValueEqual(FmTinkerer.registeredCount(), 0, "no TT means no registered costs");
        } else {
            helper.assertValueEqual(FmTinkerer.registeredCount(), 8, "TT receives eight enchantment costs");
        }
        helper.succeed();
    }

    private static void morphToolsAreRegistered(GameTestHelper helper) {
        for (var item : List.of(FmItems.MORPH_PICKAXE, FmItems.MORPH_SWORD, FmItems.MORPH_SHOVEL, FmItems.MORPH_AXE)) {
            ItemStack stack = new ItemStack(item.get());
            helper.assertValueEqual(stack.getMaxDamage(), 1500, "morph tools use elemental durability");
            helper.assertValueEqual(stack.getRarity(), Rarity.EPIC, "morph tools are epic");
            helper.assertTrue(stack.get(DataComponents.REPAIRABLE) != null, "morph tools declare a repair item");
            helper.assertTrue(stack.get(DataComponents.REPAIRABLE).isValidRepairItem(new ItemStack(FmItems.DEADLY_SHARDS.get("envy").get())), "envy shards repair morph tools");
        }
        helper.succeed();
    }

    private static void morphPhaseRotatesSets(GameTestHelper helper) {
        MorphState state = MorphState.EMPTY.store(ItemEnchantments.EMPTY, "Sharpness").advance();
        helper.assertValueEqual(state.phase(), 1, "phase advances");
        helper.assertValueEqual(state.names().get(0), "Sharpness", "the old name is kept in slot zero");
        helper.assertValueEqual(state.currentName(), "", "the new slot starts empty");
        MorphState wrapped = state.store(ItemEnchantments.EMPTY, "Smite").advance().advance();
        helper.assertValueEqual(wrapped.phase(), 0, "three phases wrap back to zero");
        helper.assertValueEqual(wrapped.currentName(), "Sharpness", "the first slot name returns");
        helper.succeed();
    }

    private static void impactCoversAThreeByThreePlane(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        BlockPos center = helper.absolutePos(new BlockPos(3, 3, 3));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(FmItems.MORPH_PICKAXE.get()));
        ItemStack stack = player.getMainHandItem();
        stack.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(FmEnchantments.IMPACT), 1);

        for (Direction face : Direction.values()) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        helper.getLevel().setBlockAndUpdate(center.offset(x, y, z), Blocks.STONE.defaultBlockState());
                    }
                }
            }

            NeoForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickBlock(
                player, center, face, PlayerInteractEvent.LeftClickBlock.Action.START));
            player.gameMode.destroyBlock(center);

            if (face.getAxis() == Direction.Axis.Y) {
                helper.assertTrue(helper.getLevel().getBlockState(center.offset(1, 0, 0)).isAir(),
                    "impact follows " + face + " and breaks the X/Z plane");
                helper.assertTrue(helper.getLevel().getBlockState(center.offset(1, 0, -1)).isAir(),
                    "impact clears the full X/Z plane for " + face);
                helper.assertFalse(helper.getLevel().getBlockState(center.above()).isAir(),
                    "impact leaves blocks along the Y axis for " + face);
                helper.assertFalse(helper.getLevel().getBlockState(center.offset(0, 1, 0)).isAir(),
                    "impact leaves blocks along the Y axis outside the X/Z plane for " + face);
            } else if (face.getAxis() == Direction.Axis.X) {
                helper.assertTrue(helper.getLevel().getBlockState(center.offset(0, 1, 0)).isAir(),
                    "impact follows " + face + " and breaks the Y/Z plane");
                helper.assertTrue(helper.getLevel().getBlockState(center.offset(0, 1, -1)).isAir(),
                    "impact clears the full Y/Z plane for " + face);
                helper.assertFalse(helper.getLevel().getBlockState(center.east()).isAir(),
                    "impact leaves blocks along the X axis for " + face);
            } else {
                helper.assertTrue(helper.getLevel().getBlockState(center.offset(1, 0, 0)).isAir(),
                    "impact follows " + face + " and breaks the X/Y plane");
                helper.assertTrue(helper.getLevel().getBlockState(center.offset(1, 1, 0)).isAir(),
                    "impact clears the full X/Y plane for " + face);
                helper.assertFalse(helper.getLevel().getBlockState(center.south()).isAir(),
                    "impact leaves blocks along the Z axis for " + face);
            }
        }
        helper.succeed();
    }

    private static void voidtouchedRepairsAndWarps(GameTestHelper helper) {
        ItemStack stack = new ItemStack(FmItems.MORPH_PICKAXE.get());
        ServerPlayer player = mockPlayer(helper);
        MorphToolItem item = (MorphToolItem) stack.getItem();
        helper.assertValueEqual(item.getWarp(stack, player), 0, "plain morph tool carries no warp");
        stack.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(FmEnchantments.VOIDTOUCHED), 1);
        helper.assertValueEqual(item.getWarp(stack, player), 1, "voidtouched morph tool carries warp 1");
        helper.succeed();
    }

    private static void morphEnchantmentsAndExclusivity(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        helper.assertValueEqual(registry.getOrThrow(FmEnchantments.CLUSTER).value().getMaxLevel(), 4, "cluster max level");
        helper.assertValueEqual(registry.getOrThrow(FmEnchantments.IMPACT).value().getMaxLevel(), 1, "impact max level");
        helper.assertValueEqual(registry.getOrThrow(FmEnchantments.VOIDTOUCHED).value().getMaxLevel(), 1, "voidtouched max level");
        helper.assertTrue(registry.getOrThrow(FmEnchantments.CLUSTER).value().isSupportedItem(new ItemStack(FmItems.MORPH_PICKAXE.get())), "cluster fits the chameleon pickaxe");
        helper.assertFalse(registry.getOrThrow(FmEnchantments.IMPACT).value().isSupportedItem(new ItemStack(FmItems.MORPH_SWORD.get())), "impact does not fit the sword");
        helper.assertFalse(Enchantment.areCompatible(
            registry.getOrThrow(FmEnchantments.CLUSTER), registry.getOrThrow(Enchantments.FORTUNE)),
            "cluster excludes fortune");
        helper.assertFalse(Enchantment.areCompatible(
            registry.getOrThrow(FmEnchantments.CONSUMING), registry.getOrThrow(Enchantments.SILK_TOUCH)),
            "consuming excludes silk touch");
        helper.assertFalse(Enchantment.areCompatible(
            registry.getOrThrow(FmEnchantments.GREEDY), registry.getOrThrow(Enchantments.LOOTING)),
            "greedy excludes looting");
        helper.succeed();
    }

    private static void clusterReplacesDropsWithClusters(GameTestHelper helper) {
        helper.assertValueEqual(FmEnchantmentEffects.clusterChance(1), 0.275F, "level 1 chance");
        helper.assertValueEqual(FmEnchantmentEffects.clusterChance(4), 0.5F, "level 4 chance");
        ItemStack replaced = thaumcraft.item.tool.SpecialMining.find(
            net.minecraft.world.level.block.Blocks.IRON_ORE.defaultBlockState(),
            new ItemStack(Items.RAW_IRON),
            1.0F,
            RandomSource.create(1L));
        helper.assertValueEqual(BuiltInRegistries.ITEM.getKey(replaced.getItem()), Identifier.parse("thaumcraft:native_iron_cluster"), "iron ore yields a native cluster");
        helper.succeed();
    }

    private static void morphResearchEntriesPresent(GameTestHelper helper) {
        ResearchItem morph = ResearchCategories.getResearch("MORPHTOOLS");
        helper.assertTrue(morph != null, "morph research must exist");
        helper.assertValueEqual(morph.warp(), 4, "morph research carries warp 4");
        helper.assertValueEqual(morph.pages().size(), 5, "morph research has text and four recipes");
        helper.assertTrue(ResearchCategories.getResearch("CLUSTER") != null, "cluster research exists");
        helper.assertTrue(ResearchCategories.getResearch("IMPACT") != null, "impact research exists");
        ResearchItem voidtouched = ResearchCategories.getResearch("VOIDTOUCHED");
        helper.assertTrue(voidtouched != null, "voidtouched research exists");
        helper.assertValueEqual(voidtouched.warp(), 3, "voidtouched research carries warp 3");
        helper.succeed();
    }
}
