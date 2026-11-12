package dev.visfox.forbiddenreforged;

import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FmBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ForbiddenMagic.MODID);

    public static final DeferredBlock<RotatedPillarBlock> TAINT_LOG =
        register("taint_log", RotatedPillarBlock::new, FmBlocks::wood);
    public static final DeferredBlock<Block> TAINT_PLANKS =
        register("taint_planks", Block::new, properties -> wood(properties).strength(2.0F, 5.0F));
    public static final DeferredBlock<Block> TAINT_STONE_SMOOTH =
        register("taint_stone_smooth", Block::new, FmBlocks::stone);
    public static final DeferredBlock<Block> TAINT_STONE_BRICK =
        register("taint_stone_brick", Block::new, FmBlocks::stone);
    public static final DeferredBlock<Block> STAR_BLOCK =
        register("star_block", Block::new, properties -> properties.mapColor(MapColor.METAL)
            .instrument(NoteBlockInstrument.PLING).strength(5.0F, 6.0F).sound(SoundType.METAL)
            .requiresCorrectToolForDrops());
    public static final DeferredBlock<LeavesBlock> TAINT_LEAVES =
        register("taint_leaves", TaintLeavesBlock::new, properties -> properties.mapColor(MapColor.COLOR_PURPLE)
            .strength(0.2F).sound(SoundType.GRASS).noOcclusion().randomTicks()
            .isValidSpawn((state, level, pos, type) -> false));
    public static final DeferredBlock<TaintSaplingBlock> TAINT_SAPLING =
        register("taint_sapling", TaintSaplingBlock::new, properties -> properties.mapColor(MapColor.COLOR_PURPLE)
            .instabreak().sound(SoundType.GRASS).randomTicks().noOcclusion());
    public static final DeferredBlock<UmbralRoseBlock> UMBRAL_ROSE =
        register("umbral_rose", UmbralRoseBlock::new, properties -> properties.mapColor(MapColor.COLOR_BLACK)
            .instabreak().sound(SoundType.GRASS).randomTicks().noOcclusion());
    public static final DeferredBlock<WrathCageBlock> WRATH_CAGE =
        register("wrath_cage", WrathCageBlock::new, properties -> properties.mapColor(MapColor.METAL)
            .strength(4.0F, 10.0F).requiresCorrectToolForDrops().noOcclusion());
    public static final DeferredBlock<ArcaneCakeBlock> ARCANE_CAKE =
        register("arcane_cake", ArcaneCakeBlock::new, properties -> properties.mapColor(MapColor.COLOR_BROWN)
            .strength(0.5F).sound(SoundType.WOOL).randomTicks().noOcclusion().noLootTable());
    public static final DeferredBlock<UmbralRoseBushBlock> UMBRAL_ROSE_BUSH =
        register("umbral_rose_bush", UmbralRoseBushBlock::new, properties -> properties.mapColor(MapColor.COLOR_BLACK)
            .instabreak().sound(SoundType.GRASS).randomTicks().noOcclusion());

    private FmBlocks() {
    }

    private static <B extends Block> DeferredBlock<B> register(
        String name,
        Function<BlockBehaviour.Properties, ? extends B> factory,
        UnaryOperator<BlockBehaviour.Properties> properties
    ) {
        return BLOCKS.register(name, () -> factory.apply(properties.apply(BlockBehaviour.Properties.of())));
    }

    private static BlockBehaviour.Properties wood(BlockBehaviour.Properties properties) {
        return properties.mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.BASS)
            .strength(2.0F, 2.0F).sound(SoundType.WOOD);
    }

    private static BlockBehaviour.Properties stone(BlockBehaviour.Properties properties) {
        return properties.mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.BASEDRUM)
            .strength(2.0F, 10.0F).sound(SoundType.STONE).requiresCorrectToolForDrops();
    }

    public static void register(net.neoforged.bus.api.IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        FmItems.ITEMS.registerSimpleBlockItem(TAINT_LOG);
        FmItems.ITEMS.registerSimpleBlockItem(TAINT_PLANKS);
        FmItems.ITEMS.registerSimpleBlockItem(TAINT_STONE_SMOOTH);
        FmItems.ITEMS.registerSimpleBlockItem(TAINT_STONE_BRICK);
        FmItems.ITEMS.registerSimpleBlockItem(STAR_BLOCK);
        FmItems.ITEMS.registerSimpleBlockItem(TAINT_LEAVES);
        FmItems.ITEMS.registerSimpleBlockItem(TAINT_SAPLING);
        FmItems.ITEMS.registerSimpleBlockItem(UMBRAL_ROSE);
        FmItems.ITEMS.registerSimpleBlockItem(UMBRAL_ROSE_BUSH);
        FmItems.ITEMS.registerSimpleBlockItem(ARCANE_CAKE);
        FmItems.ITEMS.registerSimpleBlockItem(WRATH_CAGE);
    }
}
