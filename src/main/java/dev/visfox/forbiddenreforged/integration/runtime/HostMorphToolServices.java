package dev.visfox.forbiddenreforged.integration.runtime;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Host-backed sound and area-mining behaviour for morph tools. */
public interface HostMorphToolServices {
    void playMorphCycleSound(Level level, BlockPos pos, RandomSource random);

    boolean isAreaHarvesting();

    void breakArea(
        ItemStack tool,
        ServerLevel level,
        ServerPlayer player,
        BlockPos origin,
        Predicate<BlockState> correctTool
    );
}
