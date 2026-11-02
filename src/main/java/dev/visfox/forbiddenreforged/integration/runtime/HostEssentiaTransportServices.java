package dev.visfox.forbiddenreforged.integration.runtime;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Host-backed extraction from an adjacent essentia source.
 * The host adapter validates transport support, output direction and aspect resolution.
 */
public interface HostEssentiaTransportServices {
    boolean takeOneFromOutput(BlockEntity source, Direction outputSide, ContentId aspect);
}
