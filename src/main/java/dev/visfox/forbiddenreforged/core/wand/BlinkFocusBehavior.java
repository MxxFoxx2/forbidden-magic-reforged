package dev.visfox.forbiddenreforged.core.wand;

import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
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

public final class BlinkFocusBehavior {
    @FunctionalInterface
    public interface VisConsumer {
        boolean consume(ItemStack wand, Player player, boolean apply);
    }

    private BlinkFocusBehavior() {
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

    public static InteractionResult onRightClick(
        ItemStack wand,
        Level level,
        Player player,
        boolean hellfire,
        boolean pandemonium,
        int potency,
        int enlarge,
        VisConsumer visConsumer
    ) {
        if (!(target(player) instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }
        if (!visConsumer.consume(wand, player, !level.isClientSide())) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        Vec3 targetPos = destination(blockHit);
        double range = 3.0 + 1.5 * enlarge;
        teleportSound(serverLevel, player);
        puff(serverLevel, ParticleTypes.SMOKE, player);
        AABB aabb = new AABB(targetPos.x - range, targetPos.y - range, targetPos.z - range,
            targetPos.x + range, targetPos.y + range, targetPos.z + range);
        if (hellfire) {
            float damage = 3.0F + 3.0F * potency;
            List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, aabb);
            for (LivingEntity entity : entities) {
                if (entity == player) {
                    continue;
                }
                entity.hurtServer(serverLevel, serverLevel.damageSources().source(DamageTypes.FIREBALL, player), damage);
                entity.setRemainingFireTicks((3 + 3 * potency) * 20);
                puff(serverLevel, ParticleTypes.FLAME, entity);
            }
        } else if (pandemonium) {
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
