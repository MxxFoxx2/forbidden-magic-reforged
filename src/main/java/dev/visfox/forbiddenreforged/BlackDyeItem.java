package dev.visfox.forbiddenreforged;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.DyeColor;

public class BlackDyeItem extends Item {
    public BlackDyeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, net.minecraft.world.entity.LivingEntity target, net.minecraft.world.InteractionHand hand) {
        if (target instanceof Sheep sheep) {
            if (!sheep.isSheared() && sheep.getColor() != DyeColor.BLACK) {
                if (!player.level().isClientSide()) {
                    sheep.setColor(DyeColor.BLACK);
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.interactLivingEntity(stack, player, target, hand);
    }
}
