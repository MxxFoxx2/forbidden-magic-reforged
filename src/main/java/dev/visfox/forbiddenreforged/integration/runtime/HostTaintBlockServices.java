package dev.visfox.forbiddenreforged.integration.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Host taint-block classification and taint-shovel behavior. */
public interface HostTaintBlockServices {
    boolean isTaintMaterial(BlockState state);

    boolean isTaintShovelPurificationTarget(BlockState state);

    float taintShovelMiningSpeed();

    void playTaintShovelSound(Level level, BlockPos pos);
}
