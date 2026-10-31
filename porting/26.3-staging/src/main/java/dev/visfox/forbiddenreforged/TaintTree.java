package dev.visfox.forbiddenreforged;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public final class TaintTree {
    private TaintTree() {
    }

    public static boolean grow(ServerLevel level, BlockPos pos, RandomSource random) {
        int height = 4 + random.nextInt(3);
        if (pos.getY() + height + 2 >= level.getMaxY()) {
            return false;
        }
        for (int y = 1; y <= height; y++) {
            if (!level.getBlockState(pos.above(y)).canBeReplaced()) {
                return false;
            }
        }
        BlockState log = FmBlocks.TAINT_LOG.get().defaultBlockState();
        for (int y = 0; y < height; y++) {
            level.setBlockAndUpdate(pos.above(y), log);
        }
        BlockState leaves = FmBlocks.TAINT_LEAVES.get().defaultBlockState();
        for (int layer = 0; layer < 3; layer++) {
            int radius = layer == 0 ? 2 : 1;
            int y = height - 1 + layer;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius && Math.abs(dz) == radius && layer == 2) {
                        continue;
                    }
                    BlockPos target = pos.above(y).offset(dx, 0, dz);
                    if (level.getBlockState(target).canBeReplaced()) {
                        level.setBlockAndUpdate(target, leaves);
                    }
                }
            }
        }
        level.setBlockAndUpdate(pos.above(height), leaves);
        return true;
    }
}
