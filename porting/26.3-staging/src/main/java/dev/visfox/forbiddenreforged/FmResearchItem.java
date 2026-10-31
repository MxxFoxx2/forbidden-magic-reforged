package dev.visfox.forbiddenreforged;

import net.minecraft.resources.ResourceLocation;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchItem;

public final class FmResearchItem extends ResearchItem {
    public FmResearchItem(String key, String category, AspectList tags, int column, int row, int complexity, TcStack icon) {
        super(key, category, tags, column, row, complexity, icon);
    }

    public FmResearchItem(String key, String category, AspectList tags, int column, int row, int complexity, ResourceLocation icon) {
        super(key, category, tags, column, row, complexity, icon);
    }

    @Override
    public String textKey() {
        return FmConfig.tagResearchEnabled()
            ? "forbiddenmagic.research_text." + key()
            : super.textKey();
    }
}
