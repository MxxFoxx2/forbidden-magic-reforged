package dev.visfox.forbiddenreforged.core.wand;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import net.minecraft.world.item.ItemStack;

public interface WandPartsAccess {
    /**
     * Replaces the wand rod while preserving its other parts.
     *
     * @param wand the native wand stack
     * @param rod the canonical identifier returned by {@link WandMaterialCatalog#rodId(String)}
     */
    void setRod(ItemStack wand, ContentId rod);
}
