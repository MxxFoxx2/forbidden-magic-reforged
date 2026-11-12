package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.FmConfig;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchItem;

public final class Tc4ResearchItem extends ResearchItem {
    private final String semanticKey;

    public Tc4ResearchItem(String key, String category, AspectList tags, int column, int row, int complexity, TcStack icon) {
        this(key, category, tags, column, row, complexity, icon, key);
    }

    public Tc4ResearchItem(String key, String category, AspectList tags, int column, int row, int complexity, ResourceLocation icon) {
        this(key, category, tags, column, row, complexity, icon, key);
    }

    public Tc4ResearchItem(
        String key,
        String category,
        AspectList tags,
        int column,
        int row,
        int complexity,
        TcStack icon,
        String semanticKey
    ) {
        super(key, category, tags, column, row, complexity, icon);
        this.semanticKey = semanticKey;
    }

    public Tc4ResearchItem(
        String key,
        String category,
        AspectList tags,
        int column,
        int row,
        int complexity,
        ResourceLocation icon,
        String semanticKey
    ) {
        super(key, category, tags, column, row, complexity, icon);
        this.semanticKey = semanticKey;
    }

    @Override
    public String nameKey() {
        return "tc.research_name." + semanticKey;
    }

    @Override
    public String textKey() {
        return FmConfig.tagResearchEnabled()
            ? "forbiddenmagic.research_text." + semanticKey
            : "tc.research_text." + semanticKey;
    }
}
