package dev.visfox.forbiddenreforged;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class LustShardItem extends Item {
    public LustShardItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(FmConfig.lustDisabled() ? "item.forbiddenmagic.despair_shard" : "item.forbiddenmagic.lust_shard");
    }
}
