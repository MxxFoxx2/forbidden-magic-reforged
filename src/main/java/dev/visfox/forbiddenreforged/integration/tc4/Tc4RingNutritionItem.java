package dev.visfox.forbiddenreforged.integration.tc4;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import thaumcraft.item.gear.RunicArmor;
import thaumcraft.platform.Bauble;
import thaumcraft.platform.BaubleType;

public final class Tc4RingNutritionItem extends Item implements Bauble, RunicArmor {
    public Tc4RingNutritionItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.RING;
    }

    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    @Override
    public void onWornTick(ItemStack stack, LivingEntity wearer) {
        // The original ring grants +2 food and +2 saturation when food is consumed;
        // that behavior is handled by FmToolEvents.onFoodFinished.
    }

    @Override
    public void onEquipped(ItemStack stack, LivingEntity wearer) {
    }

    @Override
    public void onUnequipped(ItemStack stack, LivingEntity wearer) {
    }
}
