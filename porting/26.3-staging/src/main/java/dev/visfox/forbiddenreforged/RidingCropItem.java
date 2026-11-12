package dev.visfox.forbiddenreforged;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.entity.golem.GolemBase;

public class RidingCropItem extends Item {
    public RidingCropItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target instanceof Player || target instanceof GolemBase) {
            target.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 1));
            target.addEffect(new MobEffectInstance(MobEffects.HASTE, 200, 1));
            target.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, 1));
        }
        super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Entity mount = player.getVehicle();
        if (mount instanceof LivingEntity livingMount) {
            if (!level.isClientSide()) {
                stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                livingMount.hurt(player.damageSources().generic(), 1.0F);
                livingMount.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 5));
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CRIT, livingMount.getX(), livingMount.getY() + 1.0, livingMount.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
                }
            }
            player.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
            return InteractionResult.SUCCESS;
        }
        return super.use(level, player, hand);
    }
}
