package dev.visfox.forbiddenreforged.integration.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Creates host-specific implementations through Minecraft's neutral block-entity base type. */
public interface HostBlockEntityServices {
    BlockEntity createWrathCageBlockEntity(BlockPos pos, BlockState state);
}
