package dev.visfox.forbiddenreforged;

import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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
            WarpHelper.addWarp(player, 1, WarpType.NORMAL);
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 0, false, false));
            if (level.getRandom().nextFloat() < 0.4F) {
                player.sendSystemMessage(Component.translatable("warp.thaumaturge.text.1").withStyle(ChatFormatting.DARK_PURPLE));
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
