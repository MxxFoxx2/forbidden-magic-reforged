package dev.visfox.forbiddenreforged;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import thaumcraft.item.bauble.VisAmuletItem;
import thaumcraft.platform.Baubles;

public class SubCollarItem extends VisAmuletItem {
    public SubCollarItem(Properties properties) {
        super(properties, false);
    }

    @Override
    public boolean canEquip(ItemStack stack, LivingEntity wearer) {
        return true;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof ServerPlayer sub) || player.level().isClientSide()) {
            return InteractionResult.PASS;
        }
        if (!Baubles.get(sub, 0).isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.collar.alreadywearing", sub.getDisplayName()));
            return InteractionResult.FAIL;
        }
        ItemStack collar = stack.copyWithCount(1);
        collar.set(FmComponents.COLLAR_OWNER.get(), player.getDisplayName().getString());
        Baubles.set(sub, 0, collar);
        stack.shrink(1);
        sub.sendSystemMessage(Component.translatable("message.collar.placescollar", player.getDisplayName()));
        player.sendSystemMessage(Component.translatable("message.collar.youplacecollar", sub.getDisplayName()));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        String owner = stack.get(FmComponents.COLLAR_OWNER.get());
        if (owner != null && !owner.isEmpty()) {
            builder.accept(Component.translatable("tooltip.collar.owner").append(" " + owner));
        }
    }
}
