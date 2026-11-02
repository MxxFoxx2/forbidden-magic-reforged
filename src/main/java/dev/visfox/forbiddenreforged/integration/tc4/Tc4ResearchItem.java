package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.FmConfig;

import net.minecraft.resources.ResourceLocation;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchItem;

public final class Tc4ResearchItem extends ResearchItem {
    public Tc4ResearchItem(String key, String category, AspectList tags, int column, int row, int complexity, TcStack icon) {
        super(key, category, tags, column, row, complexity, icon);
    }

    public Tc4ResearchItem(String key, String category, AspectList tags, int column, int row, int complexity, ResourceLocation icon) {
        super(key, category, tags, column, row, complexity, icon);
    }

    @Override
    public String textKey() {
        return FmConfig.tagResearchEnabled()
            ? "forbiddenmagic.research_text." + key()
            : super.textKey();
    }
}
