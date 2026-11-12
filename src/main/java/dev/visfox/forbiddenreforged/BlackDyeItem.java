package dev.visfox.forbiddenreforged;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class BlackDyeItem extends Item {
    public BlackDyeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Sheep sheep) || sheep.isSheared() || sheep.getColor() == DyeColor.BLACK) {
            return super.interactLivingEntity(stack, player, target, hand);
        }
        if (!player.level().isClientSide()) {
            sheep.setColor(DyeColor.BLACK);
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
