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
}
