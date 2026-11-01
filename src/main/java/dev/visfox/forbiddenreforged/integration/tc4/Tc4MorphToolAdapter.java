package dev.visfox.forbiddenreforged.integration.tc4;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.item.tool.ToolHarvest;
import thaumcraft.registry.ModSounds;

public final class Tc4MorphToolAdapter {
    private Tc4MorphToolAdapter() {
    }

    public static boolean isAreaHarvesting() {
        return ToolHarvest.isHarvesting();
    }

    public static void breakArea(
        ItemStack tool,
        ServerLevel level,
        ServerPlayer player,
        BlockPos origin,
        Predicate<ItemStack> correctTool
    ) {
        ToolHarvest.breakArea(tool, level, player, origin, correctTool, -1);
    }

    public static void playCycleSound(Level level, BlockPos pos, RandomSource random) {
        level.playSound(
            null,
            pos,
            ModSounds.WANDFAIL.get(),
            net.minecraft.sounds.SoundSource.PLAYERS,
            0.2F,
            0.2F + random.nextFloat() * 0.2F
        );
    }
}
