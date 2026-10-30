package dev.visfox.forbiddenreforged;

import com.leclowndu93150.thaumaturge.api.taint.ITaintBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;

public class TaintSaplingBlock extends SaplingBlock implements ITaintBlock {
    public TaintSaplingBlock(Properties properties) {
        super(TreeGrower.OAK, properties);
    }

    @Override
    public void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (state.getValue(SaplingBlock.STAGE) == 0) {
            level.setBlockAndUpdate(pos, state.setValue(SaplingBlock.STAGE, 1));
            return;
        }
        grow(level, pos, state, random);
    }

    private void grow(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        if (!TaintTree.grow(level, pos, random)) {
            level.setBlockAndUpdate(pos, state);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        grow(level, pos, state, random);
    }

    @Override
    public void decay(Level level, BlockPos pos, BlockState state) {
        level.removeBlock(pos, false);
    }
}
