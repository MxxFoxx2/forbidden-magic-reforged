package dev.visfox.forbiddenreforged.core.wand;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import net.minecraft.world.item.ItemStack;

public interface WandPartsAccess {
    void setRod(ItemStack wand, ContentId rod);
}
