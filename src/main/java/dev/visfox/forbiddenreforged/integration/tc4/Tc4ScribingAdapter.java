package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import thaumcraft.Config;
import thaumcraft.aspect.Aspect;
import thaumcraft.research.ResearchManager;

public final class Tc4ScribingAdapter {
    private Tc4ScribingAdapter() {
    }

    public static boolean isNoResearchDifficulty() {
        return Config.researchDifficulty() == -1;
    }

    public static List<ContentId> primalAspectIds() {
        return Aspect.getPrimalAspects().stream()
            .map(Tc4AspectAdapter::toContentId)
            .toList();
    }

    public static boolean addAspectPool(ServerPlayer player, ContentId aspect, int amount) {
        return ResearchManager.addAspectPool(player, Tc4AspectAdapter.resolveAspect(aspect), amount);
    }
}
