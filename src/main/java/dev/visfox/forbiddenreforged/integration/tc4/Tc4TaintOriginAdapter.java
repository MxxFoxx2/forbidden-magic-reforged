package dev.visfox.forbiddenreforged.integration.tc4;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.taint.TaintOriginRegistry;

public final class Tc4TaintOriginAdapter {
    private Tc4TaintOriginAdapter() {
    }

    public static void register(Block originBlock) {
        TaintOriginRegistry.register(originBlock);
    }

    public static void unregister(Block originBlock) {
        TaintOriginRegistry.unregister(originBlock);
    }

    public static boolean isOrigin(BlockState state) {
        return TaintOriginRegistry.isOrigin(state);
    }

    public static boolean spread(ServerLevel level, BlockPos origin, BlockPos target) {
        return TaintOriginRegistry.spread(level, origin, target);
    }
}
