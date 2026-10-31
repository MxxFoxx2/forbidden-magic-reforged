package dev.visfox.forbiddenreforged;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class UmbralRoseBlock extends BushBlock {
    public static final MapCodec<UmbralRoseBlock> CODEC = simpleCodec(UmbralRoseBlock::new);
    private static final VoxelShape SHAPE = box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0);

    public UmbralRoseBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends UmbralRoseBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) <= 2) {
            spread(level, pos, random);
        }
    }

    static void spread(ServerLevel level, BlockPos pos, RandomSource random) {
        int nearby = 0;
        for (BlockPos cursor : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 1, 2))) {
            if (level.getBlockState(cursor).is(FmBlocks.UMBRAL_ROSE.get()) && ++nearby >= 10) {
                return;
            }
        }
        BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
        if (level.getBlockState(target).canBeReplaced() && level.getBlockState(target.below()).is(BlockTags.DIRT)) {
            level.setBlockAndUpdate(target, FmBlocks.UMBRAL_ROSE.get().defaultBlockState());
        }
    }
}
