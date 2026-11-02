package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import thaumcraft.essentia.EssentiaTransport;

/** Performs one host-native pull from a neighboring TC4 essentia transport. */
public final class Tc4EssentiaTransportAdapter {
    private Tc4EssentiaTransportAdapter() {
    }

    public static boolean takeOneFromOutput(BlockEntity source, Direction outputSide, ContentId aspect) {
        if (!(source instanceof EssentiaTransport transport) || !transport.canOutputTo(outputSide)) {
            return false;
        }
        return transport.takeEssentia(Tc4AspectAdapter.resolveAspect(aspect), 1, outputSide) == 1;
    }
}
