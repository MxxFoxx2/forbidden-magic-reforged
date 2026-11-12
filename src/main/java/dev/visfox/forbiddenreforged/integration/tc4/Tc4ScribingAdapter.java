package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import thaumcraft.aspect.Aspect;
import thaumcraft.research.ResearchConfig;
import thaumcraft.research.ResearchManager;

public final class Tc4ScribingAdapter {
    private Tc4ScribingAdapter() {
    }

    public static boolean isNoResearchDifficulty() {
        return ResearchConfig.RESEARCH_DIFFICULTY == -1;
    }

    public static List<ContentId> primalAspectIds() {
        return Aspect.getPrimalAspects().stream()
            .map(Tc4AspectAdapter::toContentId)
            .toList();
    }

    public static boolean addAspectPool(ServerPlayer player, ContentId aspect, int amount) {
        return ResearchManager.addAspectPool(player, Tc4AspectAdapter.resolveAspect(aspect), amount);
    }

    public static net.minecraft.world.item.ItemStack spentScribingToolsStack() {
        net.minecraft.world.item.ItemStack stack =
            new net.minecraft.world.item.ItemStack(thaumcraft.registry.ModItems.SCRIBING_TOOLS.get());
        stack.setDamageValue(stack.getMaxDamage());
        return stack;
    }
}
