package dev.visfox.forbiddenreforged.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;

/**
 * Minecraft-runtime spawn orchestration for the Wrath Cage.
 *
 * This behavior intentionally uses Minecraft world/entity APIs directly:
 * the portable boundary is the magic host, not Minecraft itself.
 */
public final class WrathCageSpawnBehavior {
    private static final int MAX_NEARBY = 6;
    private static final int SPAWN_RANGE = 4;
    private static final int SPAWN_COUNT = 3;
    private static final int PLAYER_RANGE = 16;

    private WrathCageSpawnBehavior() {
    }

    /** Returns whether another spawn attempt is allowed by the legacy fuel rules. */
    public static boolean shouldAttemptSpawn(int attempt, int fuel, int cost) {
        return attempt < SPAWN_COUNT && (fuel > 0 || cost <= 0);
    }

    /** The cage stops adding entities when six matching entities already occupy its local area. */
    public static boolean hasReachedEntityLimit(int nearbyEntities) {
        return nearbyEntities >= MAX_NEARBY;
    }

    /** Preserves the legacy horizontal/vertical search bounds around the cage. */
    public static AABB nearbyBounds(BlockPos pos) {
        return new AABB(
            pos.getX() - SPAWN_RANGE,
            pos.getY() - 1,
            pos.getZ() - SPAWN_RANGE,
            pos.getX() + SPAWN_RANGE + 1,
            pos.getY() + 2,
            pos.getZ() + SPAWN_RANGE + 1
        );
    }

    /**
     * Runs one complete spawn round. Player proximity, vanilla entity creation,
     * entity-count limits, random placement, collision checks, fuel consumption,
     * and next-delay scheduling are all Minecraft runtime behavior.
     */
    public static void spawnRound(ServerLevel level, BlockPos pos, WrathCageState state, int cost) {
        if (level.getNearestPlayer(
            pos.getX() + 0.5,
            pos.getY() + 0.5,
            pos.getZ() + 0.5,
            PLAYER_RANGE,
            false
        ) == null) {
            return;
        }

        ResourceLocation id = state.mobId() == null ? null : ResourceLocation.tryParse(state.mobId());
        if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            resetDelay(level, state);
            return;
        }

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
        if (type == null) {
            resetDelay(level, state);
            return;
        }

        AABB nearby = nearbyBounds(pos);
        for (int attempt = 0; shouldAttemptSpawn(attempt, state.fuel(), cost); attempt++) {
            // Minecraft 1.21.1 exposes the spawn-aware overload with level, consumer, position and flags.
            Entity entity = type.create(level, null, pos, MobSpawnType.SPAWNER, false, false);
            if (entity == null) {
                break;
            }

            int nearbyEntities = level.getEntitiesOfClass(
                Entity.class,
                nearby,
                other -> other.getType() == type
            ).size();
            if (hasReachedEntityLimit(nearbyEntities)) {
                break;
            }

            double x = pos.getX() + 0.5
                + (level.getRandom().nextDouble() - level.getRandom().nextDouble()) * SPAWN_RANGE;
            double y = pos.getY() + level.getRandom().nextInt(3) - 1;
            double z = pos.getZ() + 0.5
                + (level.getRandom().nextDouble() - level.getRandom().nextDouble()) * SPAWN_RANGE;
            entity.moveTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);

            if (level.noCollision(entity) && !level.containsAnyLiquid(entity.getBoundingBox())) {
                level.addFreshEntity(entity);
                state.consumeFuel();
            }
        }

        resetDelay(level, state);
    }

    private static void resetDelay(ServerLevel level, WrathCageState state) {
        state.setSpawnDelay(WrathCageBehavior.nextDelay(level.getRandom().nextInt(100), state.slothful()));
    }
}
