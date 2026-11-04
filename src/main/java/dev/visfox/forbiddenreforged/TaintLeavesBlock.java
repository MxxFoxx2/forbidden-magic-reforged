package dev.visfox.forbiddenreforged;

import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class TaintLeavesBlock extends LeavesBlock {
    public TaintLeavesBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, updateTaintDistance(state, level, pos), 3);
    }

    private static BlockState updateTaintDistance(BlockState state, LevelAccessor level, BlockPos pos) {
        int i = 7;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            mutable.setWithOffset(pos, direction);
            BlockState neighbor = level.getBlockState(mutable);
            OptionalInt distance = neighbor.is(FmBlocks.TAINT_LOG.get())
                ? OptionalInt.of(0)
                : LeavesBlock.getOptionalDistanceAt(neighbor);
            i = Math.min(i, distance.orElse(7) + 1);
            if (i == 1) {
                break;
            }
        }
        return state.setValue(DISTANCE, Integer.valueOf(i));
    }
}
