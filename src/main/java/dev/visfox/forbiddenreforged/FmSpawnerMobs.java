package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;

public final class FmSpawnerMobs {
    private static final Map<String, ContentId> MOBS = Map.ofEntries(
        Map.entry("minecraft:zombie", hostAspect("corpus")),
        Map.entry("minecraft:skeleton", hostAspect("mortuus")),
        Map.entry("minecraft:creeper", hostAspect("ignis")),
        Map.entry("minecraft:horse", hostAspect("bestia")),
        Map.entry("minecraft:pig", hostAspect("bestia")),
        Map.entry("minecraft:sheep", hostAspect("pannus")),
        Map.entry("minecraft:cow", hostAspect("bestia")),
        Map.entry("minecraft:mooshroom", hostAspect("herba")),
        Map.entry("minecraft:ocelot", hostAspect("bestia")),
        Map.entry("minecraft:chicken", hostAspect("volatus")),
        Map.entry("minecraft:squid", hostAspect("sensus")),
        Map.entry("minecraft:wolf", hostAspect("bestia")),
        Map.entry("minecraft:bat", hostAspect("volatus")),
        Map.entry("minecraft:spider", hostAspect("pannus")),
        Map.entry("minecraft:slime", hostAspect("limus")),
        Map.entry("minecraft:ghast", fmAspect("infernus")),
        Map.entry("minecraft:zombified_piglin", hostAspect("lucrum")),
        Map.entry("minecraft:enderman", hostAspect("alienis")),
        Map.entry("minecraft:cave_spider", hostAspect("venenum")),
        Map.entry("minecraft:silverfish", hostAspect("lucrum")),
        Map.entry("minecraft:blaze", hostAspect("ignis")),
        Map.entry("minecraft:magma_cube", hostAspect("ignis")),
        Map.entry("minecraft:witch", hostAspect("praecantatio")),
        Map.entry("minecraft:villager", hostAspect("lucrum")),
        Map.entry("thaumcraft:firebat", hostAspect("ignis")),
        Map.entry("thaumcraft:wisp", hostAspect("auram")),
        Map.entry("thaumcraft:thaumic_slime", hostAspect("vitium")),
        Map.entry("thaumcraft:brainy_zombie", hostAspect("cognitio")),
        Map.entry("thaumcraft:giant_brainy_zombie", hostAspect("cognitio")),
        Map.entry("thaumcraft:taint_spider", hostAspect("vitium")),
        Map.entry("thaumcraft:taint_swarm", hostAspect("vitium")),
        Map.entry("thaumcraft:tainted_pig", hostAspect("vitium")),
        Map.entry("thaumcraft:tainted_sheep", hostAspect("vitium")),
        Map.entry("thaumcraft:tainted_cow", hostAspect("vitium")),
        Map.entry("thaumcraft:tainted_chicken", hostAspect("vitium")),
        Map.entry("thaumcraft:tainted_villager", hostAspect("vitium")),
        Map.entry("thaumcraft:cultist_knight", hostAspect("alienis")),
        Map.entry("thaumcraft:cultist_cleric", hostAspect("alienis")),
        Map.entry("thaumcraft:eldritch_crab", hostAspect("alienis")),
        Map.entry("thaumcraft:inhabited_zombie", hostAspect("alienis")),
        Map.entry("thaumcraft:pech", hostAspect("lucrum")),
        Map.entry("thaumcraft:eldritch_guardian", hostAspect("alienis"))
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

    private static ContentId hostAspect(String tag) {
        return new ContentId("thaumcraft", tag);
    }

    private static ContentId fmAspect(String path) {
        return new ContentId(ForbiddenMagic.MODID, path);
    }

    public static @Nullable ContentId aspect(@Nullable String id) {
        if (id == null) {
            return null;
        }
        if (id.equals("minecraft:silverfish")) {
            return hostAspect(FmConfig.silverfishEmeralds() ? "lucrum" : "bestia");
        }
        return MOBS.get(id);
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
