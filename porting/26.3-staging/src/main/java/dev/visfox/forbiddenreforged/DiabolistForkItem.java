package dev.visfox.forbiddenreforged;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DiabolistForkItem extends Item {
    public DiabolistForkItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        if (FmConfig.spork()) {
            return Component.translatable("item.forbiddenmagic.diabolist_fork_spork");
        }
        return super.getName(stack);
    }
}
