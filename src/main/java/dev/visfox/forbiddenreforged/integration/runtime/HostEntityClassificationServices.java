package dev.visfox.forbiddenreforged.integration.runtime;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;

/** Host-specific entity classification used by shared gameplay rules. */
public interface HostEntityClassificationServices {
    boolean isTaintedMob(LivingEntity entity);

    boolean isHostBoss(LivingEntity entity);

    boolean isHostGolem(LivingEntity entity);

    Set<ContentId> bossEntityIds();
}
