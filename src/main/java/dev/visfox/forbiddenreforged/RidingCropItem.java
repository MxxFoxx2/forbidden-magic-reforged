package dev.visfox.forbiddenreforged;

import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.entity.golem.GolemBase;

public class RidingCropItem extends Item {
    public RidingCropItem(Properties properties) {
        super(properties);
    }

    public static boolean dropsLustShard(int roll) {
        return roll == 1;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target instanceof AbstractHorse || target instanceof Pig) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 5));
        } else if (target instanceof Player || target instanceof GolemBase) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 1));
            target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
        }
        maybeDropLustShard(target, attacker);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 1.0F;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        Entity mount = player.getVehicle();
        if (mount instanceof LivingEntity livingMount) {
            if (!level.isClientSide()) {
                player.getItemInHand(hand).hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                livingMount.hurt(player.damageSources().generic(), 1.0F);
                livingMount.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 5));
            }
            player.swing(hand);
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
        }
        return super.use(level, player, hand);
    }

    private static void maybeDropLustShard(LivingEntity victim, LivingEntity attacker) {
        if (attacker.level().isClientSide()
            || FmConfig.lustDisabled()
            || attacker.level().dimension() != Level.NETHER
            || !dropsLustShard(attacker.getRandom().nextInt(15))) {
            return;
        }
        RandomSource random = attacker.getRandom();
        ItemEntity drop = new ItemEntity(victim.level(), victim.getX(), victim.getY(), victim.getZ(),
            new ItemStack(FmItems.DEADLY_SHARDS.get("lust").get()));
        drop.setPickUpDelay(10);
        double y = random.nextFloat() * 0.05;
        double x = (random.nextFloat() - random.nextFloat()) * 0.1;
        double z = (random.nextFloat() - random.nextFloat()) * 0.1;
        drop.setDeltaMovement(drop.getDeltaMovement().add(x, y, z));
        victim.level().addFreshEntity(drop);
    }
}
