package dev.visfox.forbiddenreforged.integration.runtime;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Host research difficulty, primal-aspect pools, and spent scribing-tool integration. */
public interface HostScribingServices {
    boolean isNoResearchDifficulty();

    List<ContentId> primalAspectIds();

    boolean addAspectPool(ServerPlayer player, ContentId aspect, int amount);

    ItemStack createSpentScribingToolsStack();
}
