package dev.visfox.forbiddenreforged;

import net.minecraft.world.level.block.Block;
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
    public static final DeferredBlock<RotatedPillarBlock> TAINT_LOG;
    public static final DeferredBlock<Block> TAINT_PLANKS;
    public static final DeferredBlock<Block> TAINT_STONE_SMOOTH;
    public static final DeferredBlock<Block> TAINT_STONE_BRICK;
    public static final DeferredBlock<Block> STAR_BLOCK;
    public static final DeferredBlock<LeavesBlock> TAINT_LEAVES;
    public static final DeferredBlock<TaintSaplingBlock> TAINT_SAPLING;
    public static final DeferredBlock<UmbralRoseBlock> UMBRAL_ROSE;
    public static final DeferredBlock<UmbralRoseBushBlock> UMBRAL_ROSE_BUSH;
    public static final DeferredBlock<ArcaneCakeBlock> ARCANE_CAKE;
    public static final DeferredBlock<WrathCageBlock> WRATH_CAGE;

    static {
        TAINT_LOG = BLOCKS.registerBlock("taint_log", RotatedPillarBlock::new,
            properties -> wood(properties, 2.0F, 2.0F));
        TAINT_PLANKS = BLOCKS.registerBlock("taint_planks", Block::new,
            properties -> wood(properties, 2.0F, 5.0F));
        TAINT_STONE_SMOOTH = BLOCKS.registerBlock("taint_stone_smooth", Block::new,
            FmBlocks::stone);
        TAINT_STONE_BRICK = BLOCKS.registerBlock("taint_stone_brick", Block::new,
            FmBlocks::stone);
        STAR_BLOCK = BLOCKS.registerBlock("star_block", Block::new,
            properties -> properties.mapColor(MapColor.METAL).instrument(NoteBlockInstrument.PLING)
                .strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());
        TAINT_LEAVES = BLOCKS.registerBlock("taint_leaves", TaintLeavesBlock::new,
            properties -> properties.mapColor(MapColor.COLOR_PURPLE).strength(0.2F).sound(SoundType.GRASS)
                .noOcclusion().isValidSpawn((state, level, pos, type) -> false).randomTicks());
        TAINT_SAPLING = BLOCKS.registerBlock("taint_sapling", TaintSaplingBlock::new,
            properties -> properties.mapColor(MapColor.COLOR_PURPLE).instabreak().sound(SoundType.GRASS).randomTicks());
        UMBRAL_ROSE = BLOCKS.registerBlock("umbral_rose", UmbralRoseBlock::new,
            properties -> properties.mapColor(MapColor.COLOR_BLACK).instabreak().sound(SoundType.GRASS).randomTicks().noCollision());
        UMBRAL_ROSE_BUSH = BLOCKS.registerBlock("umbral_rose_bush", UmbralRoseBushBlock::new,
            properties -> properties.mapColor(MapColor.COLOR_BLACK).instabreak().sound(SoundType.GRASS).randomTicks().noCollision());
        ARCANE_CAKE = BLOCKS.registerBlock("arcane_cake", ArcaneCakeBlock::new,
            properties -> properties.mapColor(MapColor.COLOR_BROWN).strength(0.5F).sound(SoundType.WOOL)
                .randomTicks().noOcclusion().noLootTable());
        WRATH_CAGE = BLOCKS.registerBlock("wrath_cage", WrathCageBlock::new,
            properties -> properties.mapColor(MapColor.METAL).strength(5.0F, 2000.0F).sound(SoundType.METAL).noOcclusion());
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

    private FmBlocks() {
    }

    private static BlockBehaviour.Properties wood(BlockBehaviour.Properties properties, float hardness, float resistance) {
        return properties.mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.BASS)
            .strength(hardness, resistance).sound(SoundType.WOOD);
    }

    private static BlockBehaviour.Properties stone(BlockBehaviour.Properties properties) {
        return properties.mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.BASEDRUM)
            .strength(2.0F, 10.0F).sound(SoundType.STONE).requiresCorrectToolForDrops();
    }
}
