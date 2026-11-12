package dev.visfox.forbiddenreforged.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.channel.embedded.EmbeddedChannel;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.util.RandomSource;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.ArcaneCakeBlock;
import dev.visfox.forbiddenreforged.FmBlocks;
import dev.visfox.forbiddenreforged.TaintTree;
import dev.visfox.forbiddenreforged.TaintLogTextureVariant;
import thaumcraft.test.TestSupport;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.aspect.Aspect;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.taint.TaintSpread;

@GameTestHolder
@PrefixGameTestTemplate(false)
public final class BlockGameTests {



    private BlockGameTests() {}


    private static ServerPlayer mockPlayer(GameTestHelper helper) {
        ServerPlayer player = TestSupport.mockServerPlayer(helper);
        player.setGameMode(GameType.SURVIVAL);
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(player.getGameProfile(), false);
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        player.connection = new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
            @Override
            public void send(Packet<?> packet) {
            }
        };
        return player;
    }

    private static JsonObject readJson(GameTestHelper helper, String path) {
        try (InputStream stream = ForbiddenMagic.class.getResourceAsStream(path)) {
            if (stream == null) {
                helper.fail("missing resource " + path);
            }
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) {
            helper.fail("could not read " + path + ": " + e);
            return new JsonObject();
        }
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void taintMaterialBlocksAreRegistered(GameTestHelper helper) {
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.TAINT_LOG.get()), ResourceLocation.parse("forbiddenmagic:taint_log"), "taint log id");
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.TAINT_PLANKS.get()), ResourceLocation.parse("forbiddenmagic:taint_planks"), "taint planks id");
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.TAINT_STONE_SMOOTH.get()), ResourceLocation.parse("forbiddenmagic:taint_stone_smooth"), "smooth stone id");
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.TAINT_STONE_BRICK.get()), ResourceLocation.parse("forbiddenmagic:taint_stone_brick"), "brick id");
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.STAR_BLOCK.get()), ResourceLocation.parse("forbiddenmagic:star_block"), "star block id");
        helper.assertFalse(new ItemStack(FmBlocks.TAINT_LOG.get().asItem()).isEmpty(), "taint log has an item");
        helper.assertFalse(new ItemStack(FmBlocks.STAR_BLOCK.get().asItem()).isEmpty(), "star block has an item");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void taintBlocksCarryToolTags(GameTestHelper helper) {
        TagKey<Block> axe = TagKey.create(Registries.BLOCK, ResourceLocation.parse("minecraft:mineable/axe"));
        TagKey<Block> pickaxe = TagKey.create(Registries.BLOCK, ResourceLocation.parse("minecraft:mineable/pickaxe"));
        TagKey<Block> beacon = TagKey.create(Registries.BLOCK, ResourceLocation.parse("minecraft:beacon_base_blocks"));
        helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.TAINT_LOG.get()).is(axe), "taint log mines with an axe");
        helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.TAINT_PLANKS.get()).is(axe), "taint planks mine with an axe");
        helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.TAINT_STONE_SMOOTH.get()).is(pickaxe), "taint stone mines with a pickaxe");
        helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.STAR_BLOCK.get()).is(beacon), "star block is a beacon base");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void taintMaterialsResistFibrousTaint(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
        for (Block block : List.of(
            FmBlocks.TAINT_LOG.get(),
            FmBlocks.TAINT_PLANKS.get(),
            FmBlocks.TAINT_STONE_SMOOTH.get(),
            FmBlocks.TAINT_STONE_BRICK.get()
        )) {
            level.setBlockAndUpdate(pos, block.defaultBlockState());
            level.setBlockAndUpdate(pos.above(), Blocks.STONE.defaultBlockState());
            helper.assertFalse(TaintSpread.spreadFibres(level, pos),
                BuiltInRegistries.BLOCK.getKey(block) + " must not be replaced by fibrous taint");
            helper.assertTrue(level.getBlockState(pos).is(block),
                BuiltInRegistries.BLOCK.getKey(block) + " remains intact after a spread attempt");
        }

        helper.assertFalse(FmBlocks.TAINT_LOG.get().defaultBlockState().is(BlockTags.LOGS),
            "taint log must not enter the host's vanilla-log crusting path");
        TagKey<Block> leafSupport = TagKey.create(Registries.BLOCK, ResourceLocation.parse("minecraft:prevents_nearby_leaf_decay"));
        helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.TAINT_LOG.get()).is(leafSupport),
            "taint log is marked as leaf support without entering the vanilla logs tag");
        helper.assertFalse(FmBlocks.TAINT_PLANKS.get().defaultBlockState().is(BlockTags.LOGS),
            "taint planks must not enter the host's vanilla-log crusting path");
        for (Block block : List.of(FmBlocks.TAINT_PLANKS.get(), FmBlocks.TAINT_STONE_SMOOTH.get(), FmBlocks.TAINT_STONE_BRICK.get())) {
            var state = block.defaultBlockState();
            helper.assertFalse(state.is(BlockTags.DIRT) || state.is(BlockTags.SAND)
                || state.is(Blocks.CLAY) || state.is(Blocks.GRASS_BLOCK),
                BuiltInRegistries.BLOCK.getKey(block) + " must not enter the host's soil conversion path");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void blocksDropThemselves(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(0, 4, 0));
        for (var block : List.of(
            FmBlocks.TAINT_LOG.get(), FmBlocks.TAINT_PLANKS.get(),
            FmBlocks.TAINT_STONE_SMOOTH.get(), FmBlocks.TAINT_STONE_BRICK.get(),
            FmBlocks.STAR_BLOCK.get())) {
            level.setBlockAndUpdate(pos, block.defaultBlockState());
            var drops = Block.getDrops(level.getBlockState(pos), level, pos, null);
            helper.assertTrue(drops.stream().anyMatch(stack -> stack.is(block.asItem())), BuiltInRegistries.BLOCK.getKey(block) + " must drop itself");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void taintSaplingAdvancesStageBeforeGrowth(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos soil = helper.absolutePos(new BlockPos(2, 4, 2));
        level.setBlockAndUpdate(soil, Blocks.DIRT.defaultBlockState());
        BlockPos sapling = soil.above();
        BlockState state = FmBlocks.TAINT_SAPLING.get().defaultBlockState();
        level.setBlockAndUpdate(sapling, state);
        FmBlocks.TAINT_SAPLING.get().advanceTree(level, sapling, state, RandomSource.create(3L));
        BlockState staged = level.getBlockState(sapling);
        helper.assertTrue(staged.is(FmBlocks.TAINT_SAPLING.get()), "first growth stage keeps the sapling");
        helper.assertValueEqual(staged.getValue(SaplingBlock.STAGE), 1, "first growth stage sets the native sapling stage");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void taintLeavesUseTaintLogAsDecaySupport(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos log = helper.absolutePos(new BlockPos(2, 4, 2));
        BlockPos leaves = log.east();
        level.setBlockAndUpdate(leaves, FmBlocks.TAINT_LEAVES.get().defaultBlockState());
        level.setBlockAndUpdate(log, FmBlocks.TAINT_LOG.get().defaultBlockState());
        helper.runAfterDelay(2, () -> {
            BlockState leafState = level.getBlockState(leaves);
            helper.assertTrue(leafState.is(FmBlocks.TAINT_LEAVES.get()), "taint leaves remain next to a taint log");
            helper.assertValueEqual(leafState.getValue(LeavesBlock.DISTANCE), 1, "taint log sets leaf support distance to one");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void taintSaplingGrowsATree(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos soil = helper.absolutePos(new BlockPos(2, 4, 2));
        level.setBlockAndUpdate(soil, Blocks.DIRT.defaultBlockState());
        BlockPos sapling = soil.above();
        for (int y = 1; y <= 8; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    level.setBlockAndUpdate(sapling.above(y).offset(x, 0, z), Blocks.AIR.defaultBlockState());
                }
            }
        }
        BlockState stagedSapling = FmBlocks.TAINT_SAPLING.get().defaultBlockState().setValue(SaplingBlock.STAGE, 1);
        level.setBlockAndUpdate(sapling, stagedSapling);
        FmBlocks.TAINT_SAPLING.get().advanceTree(level, sapling, stagedSapling, RandomSource.create(3L));
        helper.assertTrue(level.getBlockState(sapling).is(FmBlocks.TAINT_LOG.get()), "trunk base replaces the sapling");

        int canopyHeight = 0;
        for (int y = 1; y <= 8; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos cursor = sapling.above(y).offset(x, 0, z);
                    if (level.getBlockState(cursor).is(FmBlocks.TAINT_LEAVES.get())) {
                        canopyHeight = Math.max(canopyHeight, y);
                    }
                }
            }
        }
        helper.assertTrue(canopyHeight >= 4 && canopyHeight <= 6, "taint tree crown height is four to six blocks");
        helper.assertTrue(level.getBlockState(sapling.above(canopyHeight - 3).offset(2, 0, 0)).is(FmBlocks.TAINT_LEAVES.get()),
            "lower crown layer has radius two");
        helper.assertTrue(level.getBlockState(sapling.above(canopyHeight - 2).offset(2, 0, 0)).is(FmBlocks.TAINT_LEAVES.get()),
            "second crown layer has radius two");
        helper.assertTrue(level.getBlockState(sapling.above(canopyHeight - 1).offset(1, 0, 0)).is(FmBlocks.TAINT_LEAVES.get()),
            "upper crown layer has radius one");
        helper.assertTrue(level.getBlockState(sapling.above(canopyHeight).offset(1, 0, 0)).is(FmBlocks.TAINT_LEAVES.get()),
            "top crown layer has radius one");
        helper.assertTrue(level.getBlockState(sapling.above(canopyHeight).offset(1, 0, 1)).isAir(),
            "top crown corners remain empty as in the original generator");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void umbralPlantsAreRegistered(GameTestHelper helper) {
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.UMBRAL_ROSE.get()), ResourceLocation.parse("forbiddenmagic:umbral_rose"), "umbral rose id");
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.UMBRAL_ROSE_BUSH.get()), ResourceLocation.parse("forbiddenmagic:umbral_rose_bush"), "umbral rose bush id");
        TagKey<Block> flowers = TagKey.create(Registries.BLOCK, ResourceLocation.parse("minecraft:small_flowers"));
        helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.UMBRAL_ROSE.get()).is(flowers), "umbral rose is a small flower");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void arcaneCakeFeedsAndKeepsSlices(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 4, 4));
        level.setBlockAndUpdate(pos, FmBlocks.ARCANE_CAKE.get().defaultBlockState());
        helper.assertTrue(ResearchCategories.getResearch("ARCANECAKE") != null, "cake research exists");
        helper.assertTrue(TcRecipes.get("ARCANECAKE") instanceof InfusionRecipe, "cake recipe exists");
        ServerPlayer player = mockPlayer(helper);
        player.getFoodData().setFoodLevel(10);
        ArcaneCakeBlock.eat(level, pos, level.getBlockState(pos), player);
        helper.assertValueEqual(level.getBlockState(pos).getValue(ArcaneCakeBlock.SLICE), 1, "one slice eaten");
        helper.assertValueEqual(player.getFoodData().getFoodLevel(), 12, "cake restores two hunger");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void taintLogSelectsSideTexturesDeterministically(GameTestHelper helper) {
        try (InputStream stream = ForbiddenMagic.class.getResourceAsStream("/assets/forbiddenmagic/blockstates/taint_log.json")) {
            helper.assertTrue(stream != null, "taint log blockstate must ship with the mod");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (String axis : List.of("axis=x", "axis=y", "axis=z")) {
                JsonObject definition = root.getAsJsonObject("variants").getAsJsonObject(axis);
                helper.assertValueEqual(definition.get("model").getAsString(), "forbiddenmagic:block/taint_log_faces",
                    axis + " must use the coordinate-aware face model");
            }
            JsonObject facesModel = readJson(helper, "/assets/forbiddenmagic/models/block/taint_log_faces.json");
            helper.assertValueEqual(facesModel.get("loader").getAsString(), "forbiddenmagic:taint_log_faces",
                "the face model must use the coordinate-aware geometry loader");

            int[] counts = new int[3];
            for (int x = -16; x <= 16; x++) {
                for (int z = -16; z <= 16; z++) {
                    for (int side = 0; side < 6; side++) {
                        int selected = TaintLogTextureVariant.select(side, x, 7, z);
                        helper.assertTrue(selected >= 0 && selected <= 2, "face texture selector must return a known variant");
                        helper.assertValueEqual(selected, TaintLogTextureVariant.select(side, x, 7, z),
                            "face texture selection must be stable for a given side and position");
                        counts[selected]++;
                    }
                }
            }
            for (int variant = 0; variant < counts.length; variant++) {
                helper.assertTrue(counts[variant] > 0, "coordinate sample must exercise side-texture variant " + variant);
            }

            helper.assertValueEqual(TaintLogTextureVariant.select(3, 3, 7, 5), TaintLogTextureVariant.GROSS,
                "selection must use the original side + y + x*z seed");
            helper.assertValueEqual(TaintLogTextureVariant.variantForDirection(net.minecraft.core.Direction.Axis.Y,
                net.minecraft.core.Direction.DOWN, 3, 7, 5), TaintLogTextureVariant.STANDARD,
                "vertical log ends keep the end texture");
            helper.assertValueEqual(TaintLogTextureVariant.variantForDirection(net.minecraft.core.Direction.Axis.Y,
                net.minecraft.core.Direction.SOUTH, 3, 7, 5), TaintLogTextureVariant.GROSS,
                "vertical log sides use the coordinate-selected variant");
            helper.assertValueEqual(TaintLogTextureVariant.variantForDirection(net.minecraft.core.Direction.Axis.X,
                net.minecraft.core.Direction.EAST, 3, 7, 5), TaintLogTextureVariant.STANDARD,
                "horizontal log ends keep the end texture");
            helper.succeed();
        } catch (IOException e) {
            helper.fail("could not read the taint log blockstate: " + e);
        }
    }
}
