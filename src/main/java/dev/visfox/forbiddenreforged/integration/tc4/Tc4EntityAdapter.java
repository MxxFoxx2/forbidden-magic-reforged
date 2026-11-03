package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.entity.TaintedMob;
import thaumcraft.entity.boss.ThaumcraftBoss;

public final class Tc4EntityAdapter {
    private static final Map<ContentId, ContentId> ENTITY_ASPECTS = Map.ofEntries(
        Map.entry(ContentId.parse("minecraft:zombie"), aspect("corpus")),
        Map.entry(ContentId.parse("minecraft:skeleton"), aspect("mortuus")),
        Map.entry(ContentId.parse("minecraft:creeper"), aspect("ignis")),
        Map.entry(ContentId.parse("minecraft:horse"), aspect("bestia")),
        Map.entry(ContentId.parse("minecraft:pig"), aspect("bestia")),
        Map.entry(ContentId.parse("minecraft:sheep"), aspect("pannus")),
        Map.entry(ContentId.parse("minecraft:cow"), aspect("bestia")),
        Map.entry(ContentId.parse("minecraft:mooshroom"), aspect("herba")),
        Map.entry(ContentId.parse("minecraft:ocelot"), aspect("bestia")),
        Map.entry(ContentId.parse("minecraft:chicken"), aspect("volatus")),
        Map.entry(ContentId.parse("minecraft:squid"), aspect("sensus")),
        Map.entry(ContentId.parse("minecraft:wolf"), aspect("bestia")),
        Map.entry(ContentId.parse("minecraft:bat"), aspect("volatus")),
        Map.entry(ContentId.parse("minecraft:spider"), aspect("pannus")),
        Map.entry(ContentId.parse("minecraft:slime"), aspect("limus")),
        Map.entry(ContentId.parse("minecraft:ghast"), new ContentId("forbiddenmagic", "infernus")),
        Map.entry(ContentId.parse("minecraft:zombified_piglin"), aspect("lucrum")),
        Map.entry(ContentId.parse("minecraft:enderman"), aspect("alienis")),
        Map.entry(ContentId.parse("minecraft:cave_spider"), aspect("venenum")),
        Map.entry(ContentId.parse("minecraft:blaze"), aspect("ignis")),
        Map.entry(ContentId.parse("minecraft:magma_cube"), aspect("ignis")),
        Map.entry(ContentId.parse("minecraft:witch"), aspect("praecantatio")),
        Map.entry(ContentId.parse("minecraft:villager"), aspect("lucrum")),
        Map.entry(ContentId.parse("thaumcraft:firebat"), aspect("ignis")),
        Map.entry(ContentId.parse("thaumcraft:wisp"), aspect("auram")),
        Map.entry(ContentId.parse("thaumcraft:thaumic_slime"), aspect("vitium")),
        Map.entry(ContentId.parse("thaumcraft:brainy_zombie"), aspect("cognitio")),
        Map.entry(ContentId.parse("thaumcraft:giant_brainy_zombie"), aspect("cognitio")),
        Map.entry(ContentId.parse("thaumcraft:taint_spider"), aspect("vitium")),
        Map.entry(ContentId.parse("thaumcraft:taint_swarm"), aspect("vitium")),
        Map.entry(ContentId.parse("thaumcraft:tainted_pig"), aspect("vitium")),
        Map.entry(ContentId.parse("thaumcraft:tainted_sheep"), aspect("vitium")),
        Map.entry(ContentId.parse("thaumcraft:tainted_cow"), aspect("vitium")),
        Map.entry(ContentId.parse("thaumcraft:tainted_chicken"), aspect("vitium")),
        Map.entry(ContentId.parse("thaumcraft:tainted_villager"), aspect("vitium")),
        Map.entry(ContentId.parse("thaumcraft:cultist_knight"), aspect("alienis")),
        Map.entry(ContentId.parse("thaumcraft:cultist_cleric"), aspect("alienis")),
        Map.entry(ContentId.parse("thaumcraft:eldritch_crab"), aspect("alienis")),
        Map.entry(ContentId.parse("thaumcraft:inhabited_zombie"), aspect("alienis")),
        Map.entry(ContentId.parse("thaumcraft:pech"), aspect("lucrum")),
        Map.entry(ContentId.parse("thaumcraft:eldritch_guardian"), aspect("alienis"))
    );

    private static final Set<ContentId> BOSS_ENTITY_IDS = Set.of(
        ContentId.parse("thaumcraft:cultist_leader"),
        ContentId.parse("thaumcraft:eldritch_golem"),
        ContentId.parse("thaumcraft:eldritch_warden")
    );

    private static final Map<ContentId, ContentId> DEFAULT_ENTITY_ASPECTS = withSilverfishAspect("bestia");
    private static final Map<ContentId, ContentId> EMERALD_SILVERFISH_ASPECTS = withSilverfishAspect("lucrum");

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

    public static Map<ContentId, ContentId> aspectCatalog(boolean silverfishEmeralds) {
        return silverfishEmeralds ? EMERALD_SILVERFISH_ASPECTS : DEFAULT_ENTITY_ASPECTS;
    }

    public static Set<ContentId> bossEntityIds() {
        return BOSS_ENTITY_IDS;
    }

    private static Map<ContentId, ContentId> withSilverfishAspect(String aspect) {
        Map<ContentId, ContentId> result = new LinkedHashMap<>(ENTITY_ASPECTS);
        result.put(ContentId.parse("minecraft:silverfish"), aspect(aspect));
        return Map.copyOf(result);
    }

    private static ContentId aspect(String tag) {
        return new ContentId("thaumcraft", tag);
    }
}
