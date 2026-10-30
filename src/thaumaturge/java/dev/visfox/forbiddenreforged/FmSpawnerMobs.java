package dev.visfox.forbiddenreforged;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;

public final class FmSpawnerMobs {
    private static final Map<String, ResourceKey<IAspect>> MOBS = new LinkedHashMap<>();

    static {
        mob("minecraft:zombie", TTAspects.EXANIMIS);
        mob("minecraft:skeleton", TTAspects.MORTUUS);
        mob("minecraft:creeper", TTAspects.IGNIS);
        mob("minecraft:horse", TTAspects.BESTIA);
        mob("minecraft:pig", TTAspects.BESTIA);
        mob("minecraft:sheep", TTAspects.BESTIA);
        mob("minecraft:cow", TTAspects.BESTIA);
        mob("minecraft:mooshroom", TTAspects.HERBA);
        mob("minecraft:ocelot", TTAspects.BESTIA);
        mob("minecraft:chicken", TTAspects.VOLATUS);
        mob("minecraft:squid", TTAspects.SENSUS);
        mob("minecraft:wolf", TTAspects.BESTIA);
        mob("minecraft:bat", TTAspects.VOLATUS);
        mob("minecraft:spider", TTAspects.BESTIA);
        mob("minecraft:slime", TTAspects.VITIUM);
        mob("minecraft:ghast", FmAspects.INFERNUS);
        mob("minecraft:zombified_piglin", TTAspects.DESIDERIUM);
        mob("minecraft:enderman", TTAspects.ALIENIS);
        mob("minecraft:cave_spider", TTAspects.MORTUUS);
        mob("minecraft:silverfish", TTAspects.DESIDERIUM);
        mob("minecraft:blaze", TTAspects.IGNIS);
        mob("minecraft:magma_cube", TTAspects.IGNIS);
        mob("minecraft:witch", TTAspects.PRAECANTATIO);
        mob("minecraft:villager", TTAspects.DESIDERIUM);
        mob("thaumaturge:firebat", TTAspects.IGNIS);
        mob("thaumaturge:wisp", TTAspects.AURAM);
        mob("thaumaturge:thaumic_slime", TTAspects.VITIUM);
        mob("thaumaturge:brainy_zombie", TTAspects.COGNITIO);
        mob("thaumaturge:giant_brainy_zombie", TTAspects.COGNITIO);
        mob("thaumaturge:taint_crawler", TTAspects.VITIUM);
        mob("thaumaturge:taint_swarm", TTAspects.VITIUM);
        mob("thaumaturge:taint_pig", TTAspects.VITIUM);
        mob("thaumaturge:taint_sheep", TTAspects.VITIUM);
        mob("thaumaturge:taint_chicken", TTAspects.VITIUM);
        mob("thaumaturge:cultist_knight", TTAspects.ALIENIS);
        mob("thaumaturge:cultist_cleric", TTAspects.ALIENIS);
        mob("thaumaturge:eldritch_crab", TTAspects.ALIENIS);
        mob("thaumaturge:inhabited_zombie", TTAspects.ALIENIS);
        mob("thaumaturge:pech", TTAspects.DESIDERIUM);
        mob("thaumaturge:eldritch_guardian", TTAspects.ALIENIS);
    }

    private static final Set<String> BOSSES = Set.of(
        "minecraft:ender_dragon",
        "minecraft:wither",
        "thaumaturge:cultist_leader",
        "thaumaturge:eldritch_golem",
        "thaumaturge:eldritch_warden"
    );

    private FmSpawnerMobs() {
    }

    private static void mob(String id, ResourceKey<IAspect> aspect) {
        MOBS.put(id, aspect);
    }

    public static @Nullable ResourceKey<IAspect> aspect(@Nullable String id) {
        return id == null ? null : MOBS.get(id);
    }

    public static List<String> ids() {
        return List.copyOf(MOBS.keySet());
    }

    public static boolean canImprint(EntityType<?> type) {
        if (!FmConfig.wrathCage()) {
            return false;
        }
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
        return MOBS.containsKey(id) || (FmConfig.wrathCrazy() && !isBoss(type));
    }

    public static boolean isBoss(EntityType<?> type) {
        return BOSSES.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
    }

    public static String entityId(EntityType<?> type) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return id.toString();
    }
}
