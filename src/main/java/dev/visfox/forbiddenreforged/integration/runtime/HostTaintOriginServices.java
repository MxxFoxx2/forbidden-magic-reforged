package dev.visfox.forbiddenreforged.integration.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Registration and controlled propagation hooks for add-on-owned taint origins. */
public interface HostTaintOriginServices {
    void registerOrigin(Block originBlock);

    void unregisterOrigin(Block originBlock);

    boolean isOrigin(BlockState state);

    boolean spreadFromOrigin(ServerLevel level, BlockPos origin, BlockPos target);
}
