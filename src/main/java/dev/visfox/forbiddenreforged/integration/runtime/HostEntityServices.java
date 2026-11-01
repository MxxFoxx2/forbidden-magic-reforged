package dev.visfox.forbiddenreforged.integration.runtime;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;

/**
 * Host-owned entity taxonomy and aspect semantics exposed using typed, neutral identifiers.
 */
public interface HostEntityServices {
    boolean isTaintedMob(LivingEntity entity);

    boolean isHostBoss(LivingEntity entity);

    boolean isHostGolem(LivingEntity entity);

    Map<ContentId, ContentId> aspectCatalog(boolean silverfishEmeralds);

    Set<ContentId> bossEntityIds();
}
