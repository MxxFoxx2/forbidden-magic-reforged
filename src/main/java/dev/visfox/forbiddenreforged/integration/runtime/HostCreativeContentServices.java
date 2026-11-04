package dev.visfox.forbiddenreforged.integration.runtime;

import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Adds host-native sample stacks to the shared creative tab. */
public interface HostCreativeContentServices {
    void addCreativeContent(List<ItemStack> stacks);
}
