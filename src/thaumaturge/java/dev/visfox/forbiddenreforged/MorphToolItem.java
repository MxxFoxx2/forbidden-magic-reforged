package dev.visfox.forbiddenreforged;

import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MorphToolItem extends Item implements IWarpingGear {
    public MorphToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!hasTags(stack) || stack.getMaxDamage() - stack.getDamageValue() <= 5) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            MorphState state = stack.getOrDefault(FmComponents.MORPH_STATE.get(), MorphState.EMPTY);
            String current = stack.has(DataComponents.CUSTOM_NAME) ? stack.getHoverName().getString() : "";
            MorphState next = state.store(stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY), current).advance();
            stack.set(FmComponents.MORPH_STATE.get(), next);
            stack.set(DataComponents.ENCHANTMENTS, next.currentSet());
            if (next.currentName().isEmpty()) {
                stack.remove(DataComponents.CUSTOM_NAME);
            } else {
                stack.set(DataComponents.CUSTOM_NAME, Component.literal(next.currentName()).withStyle(ChatFormatting.ITALIC));
            }
            stack.hurtAndBreak(5, player, EquipmentSlot.MAINHAND);
            level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS,
                0.2F, 0.2F + player.getRandom().nextFloat() * 0.2F);
        }
        player.swing(hand, true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, EquipmentSlot slot) {
        if (!(owner instanceof LivingEntity) || !stack.isDamaged() || owner.tickCount % 10 != 0) {
            return;
        }
        if (FmEnchantments.level(level.registryAccess(), stack, FmEnchantments.VOIDTOUCHED) > 0) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public int warp(ItemStack stack, LivingEntity wearer) {
        return FmEnchantments.level(wearer.level().registryAccess(), stack, FmEnchantments.VOIDTOUCHED) > 0 ? 1 : 0;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
        if (!level.isClientSide()
            && owner instanceof ServerPlayer player
            && level instanceof ServerLevel serverLevel
            && FmEnchantments.level(serverLevel.registryAccess(), stack, FmEnchantments.IMPACT) > 0
            && stack.isCorrectToolForDrops(state)) {
            breakArea(serverLevel, player, pos, stack);
        }
        return super.mineBlock(stack, level, state, pos, owner);
    }

    private static void breakArea(ServerLevel level, ServerPlayer player, BlockPos center, ItemStack stack) {
        for (BlockPos target : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            if (target.equals(center) || !level.hasChunkAt(target)) {
                continue;
            }
            BlockState state = level.getBlockState(target);
            if (state.isAir() || state.getDestroySpeed(level, target) < 0 || !stack.isCorrectToolForDrops(state)) {
                continue;
            }
            level.destroyBlock(target, true, player);
        }
    }

    private static boolean hasTags(ItemStack stack) {
        return stack.has(FmComponents.MORPH_STATE.get())
            || stack.has(DataComponents.ENCHANTMENTS)
            || stack.has(DataComponents.CUSTOM_NAME);
    }
}
