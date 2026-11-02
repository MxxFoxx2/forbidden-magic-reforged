package dev.visfox.forbiddenreforged.integration.runtime;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import net.minecraft.world.item.ItemStack;

/** Host-backed operations that add semantic aspects to wand and real Vis stores. */
public interface HostWandVisServices {
    void addWandVis(ItemStack stack, ContentId aspect, int amount, boolean apply);

    void addRealVis(ItemStack stack, ContentId aspect, int amount, boolean apply);
}
