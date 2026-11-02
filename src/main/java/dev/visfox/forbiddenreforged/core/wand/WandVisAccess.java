package dev.visfox.forbiddenreforged.core.wand;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public interface WandVisAccess {
    List<ContentId> primalAspects();

    int maximumVis(ItemStack wand);

    int vis(ItemStack wand, ContentId aspect);

    void addVis(ItemStack wand, ContentId aspect, int amount, boolean apply);

    void addRealVis(ItemStack wand, ContentId aspect, int amount, boolean apply);

    void fill(ItemStack wand);
}
