package dev.visfox.forbiddenreforged.integration.runtime;

import dev.visfox.forbiddenreforged.core.wand.BlinkFocusRules;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Host-backed focus upgrade inspection and Vis payment operations. */
public interface HostFocusServices {
    boolean hasFocusUpgrade(ItemStack focus, BlinkFocusRules.Upgrade upgrade);
    boolean consumeFocusVis(ItemStack wand, Player player, List<BlinkFocusRules.VisCost> costs, boolean apply);
}
