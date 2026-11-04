package dev.visfox.forbiddenreforged.integration.runtime;

import dev.visfox.forbiddenreforged.core.wand.WandVisAccess;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import net.minecraft.world.item.ItemStack;

/**
 * Full semantic Vis access for wands plus the separate item-backed real-Vis
 * storage operation used by accessories and other non-wand items.
 */
public interface HostWandVisServices extends WandVisAccess {
    /** Compatibility name for consumers that add stored Vis to a wand. */
    default void addWandVis(ItemStack wand, ContentId aspect, int amount, boolean apply) {
        addVis(wand, aspect, amount, apply);
    }

    /** Adds real Vis to a non-wand item's own Vis storage. */
    void addItemRealVis(ItemStack stack, ContentId aspect, int amount, boolean apply);
}
