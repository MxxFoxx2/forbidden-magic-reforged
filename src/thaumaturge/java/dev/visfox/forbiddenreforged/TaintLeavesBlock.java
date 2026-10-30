package dev.visfox.forbiddenreforged;

import com.leclowndu93150.thaumaturge.api.taint.ITaintBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class TaintLeavesBlock extends LeavesBlock implements ITaintBlock {
    public static final MapCodec<TaintLeavesBlock> CODEC = simpleCodec(TaintLeavesBlock::new);

    public TaintLeavesBlock(Properties properties) {
        super(0.0F, properties);
    }

    @Override
    public MapCodec<TaintLeavesBlock> codec() {
        return CODEC;
    }

    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
    }

    @Override
    public void decay(Level level, BlockPos pos, BlockState state) {
        level.removeBlock(pos, false);
    }
}
