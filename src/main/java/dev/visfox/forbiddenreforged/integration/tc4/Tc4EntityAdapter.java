package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.entity.TaintedMob;
import thaumcraft.entity.boss.ThaumcraftBoss;

public final class Tc4EntityAdapter {
    private static final Map<String, ContentId> ENTITY_ASPECTS = Map.ofEntries(
        Map.entry("minecraft:zombie", aspect("corpus")),
        Map.entry("minecraft:skeleton", aspect("mortuus")),
        Map.entry("minecraft:creeper", aspect("ignis")),
        Map.entry("minecraft:horse", aspect("bestia")),
        Map.entry("minecraft:pig", aspect("bestia")),
        Map.entry("minecraft:sheep", aspect("pannus")),
        Map.entry("minecraft:cow", aspect("bestia")),
        Map.entry("minecraft:mooshroom", aspect("herba")),
        Map.entry("minecraft:ocelot", aspect("bestia")),
        Map.entry("minecraft:chicken", aspect("volatus")),
        Map.entry("minecraft:squid", aspect("sensus")),
        Map.entry("minecraft:wolf", aspect("bestia")),
        Map.entry("minecraft:bat", aspect("volatus")),
        Map.entry("minecraft:spider", aspect("pannus")),
        Map.entry("minecraft:slime", aspect("limus")),
        Map.entry("minecraft:ghast", new ContentId("forbiddenmagic", "infernus")),
        Map.entry("minecraft:zombified_piglin", aspect("lucrum")),
        Map.entry("minecraft:enderman", aspect("alienis")),
        Map.entry("minecraft:cave_spider", aspect("venenum")),
        Map.entry("minecraft:blaze", aspect("ignis")),
        Map.entry("minecraft:magma_cube", aspect("ignis")),
        Map.entry("minecraft:witch", aspect("praecantatio")),
        Map.entry("minecraft:villager", aspect("lucrum")),
        Map.entry("thaumcraft:firebat", aspect("ignis")),
        Map.entry("thaumcraft:wisp", aspect("auram")),
        Map.entry("thaumcraft:thaumic_slime", aspect("vitium")),
        Map.entry("thaumcraft:brainy_zombie", aspect("cognitio")),
        Map.entry("thaumcraft:giant_brainy_zombie", aspect("cognitio")),
        Map.entry("thaumcraft:taint_spider", aspect("vitium")),
        Map.entry("thaumcraft:taint_swarm", aspect("vitium")),
        Map.entry("thaumcraft:tainted_pig", aspect("vitium")),
        Map.entry("thaumcraft:tainted_sheep", aspect("vitium")),
        Map.entry("thaumcraft:tainted_cow", aspect("vitium")),
        Map.entry("thaumcraft:tainted_chicken", aspect("vitium")),
        Map.entry("thaumcraft:tainted_villager", aspect("vitium")),
        Map.entry("thaumcraft:cultist_knight", aspect("alienis")),
        Map.entry("thaumcraft:cultist_cleric", aspect("alienis")),
        Map.entry("thaumcraft:eldritch_crab", aspect("alienis")),
        Map.entry("thaumcraft:inhabited_zombie", aspect("alienis")),
        Map.entry("thaumcraft:pech", aspect("lucrum")),
        Map.entry("thaumcraft:eldritch_guardian", aspect("alienis"))
    );

    private static final Set<String> BOSS_ENTITY_IDS = Set.of(
        "thaumcraft:cultist_leader",
        "thaumcraft:eldritch_golem",
        "thaumcraft:eldritch_warden"
    );

    private static final Map<String, ContentId> DEFAULT_ENTITY_ASPECTS = withSilverfishAspect("bestia");
    private static final Map<String, ContentId> EMERALD_SILVERFISH_ASPECTS = withSilverfishAspect("lucrum");

    private Tc4EntityAdapter() {
    }

    public static boolean isTaintedMob(LivingEntity entity) {
        return entity instanceof TaintedMob;
    }

    public static boolean isHostBoss(LivingEntity entity) {
        return entity instanceof ThaumcraftBoss;
    }

    public static boolean isHostGolem(LivingEntity entity) {
        return entity instanceof thaumcraft.entity.golem.GolemBase;
    }

    public static Map<String, ContentId> aspectCatalog(boolean silverfishEmeralds) {
        return silverfishEmeralds ? EMERALD_SILVERFISH_ASPECTS : DEFAULT_ENTITY_ASPECTS;
    }

    public static Set<String> bossEntityIds() {
        return BOSS_ENTITY_IDS;
    }

    private static Map<String, ContentId> withSilverfishAspect(String aspect) {
        Map<String, ContentId> result = new LinkedHashMap<>(ENTITY_ASPECTS);
        result.put("minecraft:silverfish", aspect(aspect));
        return Map.copyOf(result);
    }

    private static ContentId aspect(String tag) {
        return new ContentId("thaumcraft", tag);
    }
}
