package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;

public final class FmSpawnerMobs {
    private static final Set<String> VANILLA_BOSSES = Set.of(
        "minecraft:ender_dragon",
        "minecraft:wither"
    );

    private FmSpawnerMobs() {
    }

    public static @Nullable ContentId aspect(@Nullable String id) {
        if (id == null) {
            return null;
        }
        return HostAdapterRegistry.requireEntities()
            .aspectCatalog(FmConfig.silverfishEmeralds())
            .get(id);
    }

    public static List<String> ids() {
        return List.copyOf(
            HostAdapterRegistry.requireEntities()
                .aspectCatalog(FmConfig.silverfishEmeralds())
                .keySet()
        );
    }

    public static boolean canImprint(EntityType<?> type) {
        if (!FmConfig.wrathCage()) {
            return false;
        }
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
        boolean hasAspect = HostAdapterRegistry.requireEntities()
            .aspectCatalog(FmConfig.silverfishEmeralds())
            .containsKey(id);
        return hasAspect || (FmConfig.wrathCrazy() && !isBoss(type));
    }

    public static boolean isBoss(EntityType<?> type) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
        return VANILLA_BOSSES.contains(id)
            || HostAdapterRegistry.requireEntities().bossEntityIds().contains(id);
    }
}
