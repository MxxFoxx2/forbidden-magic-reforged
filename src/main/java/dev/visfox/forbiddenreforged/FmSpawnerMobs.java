package dev.visfox.forbiddenreforged;

import java.util.List;
import java.util.Set;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import javax.annotation.Nullable;

public final class FmSpawnerMobs {
    private static final Set<String> MOBS = Set.of(
        "minecraft:zombie",
        "minecraft:skeleton",
        "minecraft:creeper",
        "minecraft:horse",
        "minecraft:pig",
        "minecraft:sheep",
        "minecraft:cow",
        "minecraft:mooshroom",
        "minecraft:ocelot",
        "minecraft:chicken",
        "minecraft:squid",
        "minecraft:wolf",
        "minecraft:bat",
        "minecraft:spider",
        "minecraft:slime",
        "minecraft:ghast",
        "minecraft:zombified_piglin",
        "minecraft:enderman",
        "minecraft:cave_spider",
        "minecraft:silverfish",
        "minecraft:blaze",
        "minecraft:magma_cube",
        "minecraft:witch",
        "minecraft:villager",
        "thaumcraft:firebat",
        "thaumcraft:wisp",
        "thaumcraft:thaumic_slime",
        "thaumcraft:brainy_zombie",
        "thaumcraft:giant_brainy_zombie",
        "thaumcraft:taint_spider",
        "thaumcraft:taint_swarm",
        "thaumcraft:tainted_pig",
        "thaumcraft:tainted_sheep",
        "thaumcraft:tainted_cow",
        "thaumcraft:tainted_chicken",
        "thaumcraft:tainted_villager",
        "thaumcraft:cultist_knight",
        "thaumcraft:cultist_cleric",
        "thaumcraft:eldritch_crab",
        "thaumcraft:inhabited_zombie",
        "thaumcraft:pech",
        "thaumcraft:eldritch_guardian"
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

    public static @Nullable ContentId aspectId(@Nullable String id) {
        if (id == null) {
            return null;
        }
        return HostAdapterRegistry.requireEntityAspects()
            .aspectCatalog(FmConfig.silverfishEmeralds())
            .get(ContentId.parse(id));
    }

    public static List<String> ids() {
        return List.copyOf(MOBS);
    }

    public static boolean canImprint(EntityType<?> type) {
        if (!FmConfig.wrathCage()) {
            return false;
        }
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
        return MOBS.contains(id) || (FmConfig.wrathCrazy() && !isBoss(type));
    }

    public static boolean isBoss(EntityType<?> type) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
        if (id.equals("minecraft:ender_dragon") || id.equals("minecraft:wither")) {
            return true;
        }
        return HostAdapterRegistry.requireEntityClassification()
            .bossEntityIds()
            .contains(ContentId.parse(id));
    }
}
