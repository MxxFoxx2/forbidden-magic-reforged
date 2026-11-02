package dev.visfox.forbiddenreforged.integration.runtime;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

/** Host research difficulty and player aspect-pool integration. */
public interface HostScribingServices {
    boolean isNoResearchDifficulty();

    List<ContentId> primalAspectIds();

    boolean addAspectPool(ServerPlayer player, ContentId aspect, int amount);
}
