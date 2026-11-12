package dev.visfox.forbiddenreforged;

import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;

public final class FmSpawnerMobs {
    private static final Map<String, Aspect> MOBS = Map.ofEntries(
        Map.entry("minecraft:zombie", Aspect.FLESH),
        Map.entry("minecraft:skeleton", Aspect.DEATH),
        Map.entry("minecraft:creeper", Aspect.FIRE),
        Map.entry("minecraft:horse", Aspect.BEAST),
        Map.entry("minecraft:pig", Aspect.BEAST),
        Map.entry("minecraft:sheep", Aspect.CLOTH),
        Map.entry("minecraft:cow", Aspect.BEAST),
        Map.entry("minecraft:mooshroom", Aspect.PLANT),
        Map.entry("minecraft:ocelot", Aspect.BEAST),
        Map.entry("minecraft:chicken", Aspect.FLIGHT),
        Map.entry("minecraft:squid", Aspect.SENSES),
        Map.entry("minecraft:wolf", Aspect.BEAST),
        Map.entry("minecraft:bat", Aspect.FLIGHT),
        Map.entry("minecraft:spider", Aspect.CLOTH),
        Map.entry("minecraft:slime", Aspect.SLIME),
        Map.entry("minecraft:ghast", FmAspects.NETHER),
        Map.entry("minecraft:zombified_piglin", Aspect.GREED),
        Map.entry("minecraft:enderman", Aspect.ELDRITCH),
        Map.entry("minecraft:cave_spider", Aspect.POISON),
        Map.entry("minecraft:silverfish", Aspect.GREED),
        Map.entry("minecraft:blaze", Aspect.FIRE),
        Map.entry("minecraft:magma_cube", Aspect.FIRE),
        Map.entry("minecraft:witch", Aspect.MAGIC),
        Map.entry("minecraft:villager", Aspect.GREED),
        Map.entry("thaumcraft:firebat", Aspect.FIRE),
        Map.entry("thaumcraft:wisp", Aspect.AURA),
        Map.entry("thaumcraft:thaumic_slime", Aspect.TAINT),
        Map.entry("thaumcraft:brainy_zombie", Aspect.MIND),
        Map.entry("thaumcraft:giant_brainy_zombie", Aspect.MIND),
        Map.entry("thaumcraft:taint_spider", Aspect.TAINT),
        Map.entry("thaumcraft:taint_swarm", Aspect.TAINT),
        Map.entry("thaumcraft:tainted_pig", Aspect.TAINT),
        Map.entry("thaumcraft:tainted_sheep", Aspect.TAINT),
        Map.entry("thaumcraft:tainted_cow", Aspect.TAINT),
        Map.entry("thaumcraft:tainted_chicken", Aspect.TAINT),
        Map.entry("thaumcraft:tainted_villager", Aspect.TAINT),
        Map.entry("thaumcraft:cultist_knight", Aspect.ELDRITCH),
        Map.entry("thaumcraft:cultist_cleric", Aspect.ELDRITCH),
        Map.entry("thaumcraft:eldritch_crab", Aspect.ELDRITCH),
        Map.entry("thaumcraft:inhabited_zombie", Aspect.ELDRITCH),
        Map.entry("thaumcraft:pech", Aspect.GREED),
        Map.entry("thaumcraft:eldritch_guardian", Aspect.ELDRITCH)
    );

    private static final Set<String> BOSSES = Set.of(
        "minecraft:ender_dragon",
        "minecraft:wither",
        "thaumcraft:cultist_leader",
        "thaumcraft:eldritch_golem",
        "thaumcraft:eldritch_warden"
    );

    private FmSpawnerMobs() {
    }

    public static @Nullable Aspect aspect(@Nullable String id) {
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
}
