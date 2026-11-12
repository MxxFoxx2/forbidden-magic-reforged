package dev.visfox.forbiddenreforged;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.research.Warp;
import thaumcraft.research.WarpType;

public class TaintFruitItem extends Item {
    private static final FoodProperties TAINT_FRUIT_FOOD = new FoodProperties.Builder()
        .nutrition(4)
        .saturationModifier(0.8F)
        .build();

    public TaintFruitItem(Properties properties) {
        super(properties.food(TAINT_FRUIT_FOOD));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            Warp.add(player, 1, WarpType.STICKY);
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 0, false, false));
            if (level.getRandom().nextFloat() < 0.4F) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("warp.text.15").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
