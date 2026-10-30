package dev.visfox.forbiddenreforged;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class PrimewellItem extends Item {
    private static final int MAX_DAMAGE = 100;

    public PrimewellItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return MAX_DAMAGE;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, EquipmentSlot slot) {
        if (stack.isDamaged() && owner.tickCount % 10 == 0) {
            stack.setDamageValue(0);
        }
    }
}
