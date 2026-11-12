package dev.visfox.forbiddenreforged.integration.tc4;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.blockentity.TcBlockEntity;

/** TC4 block-entity lifecycle hooks for host-owned block entities. */
public final class Tc4BlockEntityAdapter {
    private Tc4BlockEntityAdapter() {
    }

    public static void beforeRemoveWrathCage(BlockState state, Level level, BlockPos pos, BlockState newState) {
        TcBlockEntity.beforeRemove(state, level, pos, newState);
    }
}
