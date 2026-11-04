package dev.visfox.forbiddenreforged.integration.runtime;

import dev.visfox.forbiddenreforged.common.WrathCageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Creates host-specific implementations behind shared block/entity types. */
public interface HostBlockEntityServices {
    WrathCageBlockEntity createWrathCageBlockEntity(BlockPos pos, BlockState state);
}
