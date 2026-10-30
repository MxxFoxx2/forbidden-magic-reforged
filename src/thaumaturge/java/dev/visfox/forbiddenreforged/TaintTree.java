package dev.visfox.forbiddenreforged;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public final class TaintTree {
    private TaintTree() {
    }

    public static boolean grow(ServerLevel level, BlockPos pos, RandomSource random) {
        int height = 4 + random.nextInt(3);
        if (pos.getY() < level.getMinY() || pos.getY() + height + 2 >= level.getMaxY()) {
            return false;
        }
        if (!FmBlocks.TAINT_SAPLING.get().defaultBlockState().canSurvive(level, pos)) {
            return false;
        }

        for (int y = 0; y <= height + 1; y++) {
            int radius = y == 0 ? 0 : y >= height - 1 ? 2 : 1;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos target = pos.above(y).offset(x, 0, z);
                    BlockState state = level.getBlockState(target);
                    if (!(state.isAir() || state.is(BlockTags.LEAVES) || state.canBeReplaced())) {
                        return false;
                    }
                }
            }
        }

        BlockState leaves = FmBlocks.TAINT_LEAVES.get().defaultBlockState();
        for (int y = height - 3; y <= height; y++) {
            int relativeY = y - height;
            int radius = 1 - relativeY / 2;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) != radius || Math.abs(z) != radius
                        || random.nextInt(2) != 0 && relativeY != 0) {
                        BlockPos target = pos.above(y).offset(x, 0, z);
                        BlockState state = level.getBlockState(target);
                        if (state.isAir() || state.is(BlockTags.LEAVES)) {
                            level.setBlockAndUpdate(target, leaves);
                        }
                    }
                }
            }
        }

        BlockState log = FmBlocks.TAINT_LOG.get().defaultBlockState();
        for (int y = 0; y < height; y++) {
            BlockPos target = pos.above(y);
            BlockState state = level.getBlockState(target);
            if (state.isAir() || state.is(BlockTags.LEAVES)) {
                level.setBlockAndUpdate(target, log);
            }
        }
        return true;
    }
}
