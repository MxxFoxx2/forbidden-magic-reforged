package dev.visfox.forbiddenreforged.integration.runtime;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Creates and operates the selected host's Wrath Cage through Minecraft's neutral block-entity base type. */
public interface HostBlockEntityServices {
    BlockEntity createWrathCageBlockEntity(BlockPos pos, BlockState state);

    @Nullable String wrathCageMobId(BlockEntity cage);

    void setWrathCageMobId(BlockEntity cage, @Nullable String mobId);

    void cycleWrathCageMode(BlockEntity cage);

    void tickWrathCage(Level level, BlockPos pos, BlockState state, BlockEntity cage);

    /** Runs host-specific removal callbacks for the Wrath Cage before Minecraft removes its block entity. */
    void beforeRemoveWrathCage(BlockState state, Level level, BlockPos pos, BlockState newState);
}
