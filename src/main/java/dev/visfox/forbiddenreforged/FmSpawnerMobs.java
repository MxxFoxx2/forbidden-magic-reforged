package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;

public final class FmSpawnerMobs {
    private static final Set<ContentId> VANILLA_BOSSES = Set.of(
        ContentId.parse("minecraft:ender_dragon"),
        ContentId.parse("minecraft:wither")
    );

    private FmSpawnerMobs() {
    }

    public static @Nullable ContentId aspect(@Nullable String id) {
        if (id == null) {
            return null;
        }
        try {
            return HostAdapterRegistry.requireEntityAspects()
                .aspectCatalog(FmConfig.silverfishEmeralds())
                .get(ContentId.parse(id));
        } catch (IllegalArgumentException invalidEntityId) {
            return null;
        }
    }

    public static List<String> ids() {
        return HostAdapterRegistry.requireEntityAspects()
            .aspectCatalog(FmConfig.silverfishEmeralds())
            .keySet()
            .stream()
            .sorted()
            .map(ContentId::toString)
            .toList();
    }

    public static boolean canImprint(EntityType<?> type) {
        if (!FmConfig.wrathCage()) {
            return false;
        }
        ContentId id = ContentId.parse(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        boolean hasAspect = HostAdapterRegistry.requireEntityAspects()
            .aspectCatalog(FmConfig.silverfishEmeralds())
            .containsKey(id);
        return hasAspect || (FmConfig.wrathCrazy() && !isBoss(type));
    }

    public static boolean isBoss(EntityType<?> type) {
        ContentId id = ContentId.parse(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        return VANILLA_BOSSES.contains(id)
            || HostAdapterRegistry.requireEntityClassification().bossEntityIds().contains(id);
    }
}
