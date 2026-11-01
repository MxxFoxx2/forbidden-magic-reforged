package dev.visfox.forbiddenreforged.integration.tc4;

import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.ModBlocks;
import thaumcraft.taint.TaintBlock;
import thaumcraft.taint.TaintFibresBlock;

public final class Tc4TaintAdapter {
    private Tc4TaintAdapter() {
    }

    public static boolean isHostTaintMaterial(BlockState state) {
        return state.getBlock() instanceof TaintBlock
            || state.getBlock() instanceof TaintFibresBlock
            || state.is(ModBlocks.FLUX_GOO.get())
            || state.is(ModBlocks.FLUX_GAS.get());
    }

    public static boolean isTaintShovelPurificationTarget(BlockState state) {
        return state.is(ModBlocks.FLUX_GOO.get()) || state.is(ModBlocks.FLUX_GAS.get());
    }

    public static float taintShovelMiningSpeed() {
        return thaumcraft.item.gear.TcMaterials.ELEMENTAL_TOOL.miningSpeed();
    }

    public static void playTaintShovelSound(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        level.playSound(
            null,
            pos,
            thaumcraft.registry.ModSounds.WANDFAIL.get(),
            net.minecraft.sounds.SoundSource.PLAYERS,
            0.2F,
            0.2F + level.getRandom().nextFloat() * 0.2F
        );
    }
}
