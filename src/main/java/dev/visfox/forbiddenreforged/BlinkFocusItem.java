package dev.visfox.forbiddenreforged;

import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.Wands;
import thaumcraft.wand.focus.FocusItem;
import thaumcraft.wand.focus.FocusUpgradeType;

public class BlinkFocusItem extends FocusItem {
    private static final AspectList COST_BASE = new AspectList().add(Aspect.ENTROPY, 300);
    private static final AspectList COST_HELLFIRE = new AspectList().add(Aspect.ENTROPY, 300).add(Aspect.FIRE, 100);
    private static final AspectList COST_PANDEMONIUM = new AspectList().add(Aspect.ENTROPY, 300).add(Aspect.ORDER, 100);

    public static final FocusUpgradeType HELLFIRE = new FocusUpgradeType(100, "hellfire", ForbiddenMagic.id("textures/misc/hellfire.png"), new AspectList().add(FmAspects.NETHER, 1));
    public static final FocusUpgradeType PANDEMONIUM = new FocusUpgradeType(101, "pandemonium", ForbiddenMagic.id("textures/misc/pandemonium.png"), new AspectList().add(Aspect.DARKNESS, 1));

    public BlinkFocusItem(Properties properties) {
        super(properties);
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "BL" + super.getSortingHelper(focus);
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0x909090;
    }

    @Override
    public @Nullable ResourceLocation getFocusTexture(ItemStack focus) {
        return ForbiddenMagic.id("item/blink_focus");
    }

    @Override
    public @Nullable ResourceLocation getOrnamentTexture(ItemStack focus) {
        return ForbiddenMagic.id("item/blink_focus_ornament");
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return this.isUpgradedWith(focus, HELLFIRE) ? COST_HELLFIRE : this.isUpgradedWith(focus, PANDEMONIUM) ? COST_PANDEMONIUM : COST_BASE;
    }

    @Override
    public @Nullable FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        switch (rank) {
            case 1:
                return new FocusUpgradeType[]{FocusUpgradeType.FRUGAL};
            case 2:
                return new FocusUpgradeType[]{HELLFIRE, PANDEMONIUM, FocusUpgradeType.FRUGAL};
            case 3:
            case 4:
            case 5:
                if (this.isUpgradedWith(focus, HELLFIRE)) {
                    return new FocusUpgradeType[]{FocusUpgradeType.POTENCY, FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE};
                } else if (this.isUpgradedWith(focus, PANDEMONIUM)) {
                    return new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE};
                } else {
                    return new FocusUpgradeType[]{FocusUpgradeType.FRUGAL};
                }
            default:
                return null;
        }
    }

    public static HitResult target(Player player) {
        return player.pick(128.0, 1.0F, false);
    }

    public static Vec3 destination(BlockHitResult hit) {
        Vec3 point = hit.getLocation();
        double x = point.x;
        double y = point.y;
        double z = point.z;
        switch (hit.getDirection()) {
            case DOWN -> y -= 2.0;
            case NORTH -> z -= 0.5;
            case SOUTH -> z += 0.5;
            case WEST -> x -= 0.5;
            case EAST -> x += 0.5;
            default -> {
            }
        }
        return new Vec3(x, y, z);
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult ignored) {
        if (!(target(player) instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }
        WandItem item = (WandItem) wand.getItem();
        if (!item.consumeAllVis(wand, player, this.getVisCost(wand), !level.isClientSide(), false)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        Vec3 targetPos = destination(blockHit);
        double range = 3.0 + 1.5 * Wands.getFocusEnlarge(wand);
        ItemStack focus = Wands.getFocusItem(wand);
        teleportSound(serverLevel, player);
        puff(serverLevel, ParticleTypes.SMOKE, player);
        AABB aabb = new AABB(targetPos.x - range, targetPos.y - range, targetPos.z - range,
            targetPos.x + range, targetPos.y + range, targetPos.z + range);
        if (this.isUpgradedWith(focus, HELLFIRE)) {
            int potency = Wands.getFocusPotency(wand);
            float damage = 3.0F + 3.0F * potency;
            List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, aabb);
            for (LivingEntity entity : entities) {
                if (entity == player) {
                    continue;
                }
                entity.hurt(serverLevel.damageSources().source(DamageTypes.FIREBALL, player), damage);
                entity.setRemainingFireTicks((3 + 3 * potency) * 20);
                puff(serverLevel, ParticleTypes.FLAME, entity);
            }
        } else if (this.isUpgradedWith(focus, PANDEMONIUM)) {
            List<LivingEntity> enemies = serverLevel.getEntitiesOfClass(
                LivingEntity.class, aabb, entity -> entity instanceof Enemy);
            for (LivingEntity enemy : enemies) {
                enemy.teleportTo(player.getX(), player.getY(), player.getZ());
                puff(serverLevel, ParticleTypes.SMOKE, enemy);
            }
        }
        player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
        teleportSound(serverLevel, player);
        puff(serverLevel, ParticleTypes.FLAME, player);
        return InteractionResult.SUCCESS;
    }

    private static void teleportSound(ServerLevel level, Player player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void puff(ServerLevel level, SimpleParticleType type, LivingEntity entity) {
        level.sendParticles(type,
            entity.getX(), entity.getY() + entity.getBbHeight() * 0.5 - 0.25, entity.getZ(),
            8, entity.getBbWidth() * 0.5, entity.getBbHeight() * 0.25, entity.getBbWidth() * 0.5, 0.0);
    }
}
