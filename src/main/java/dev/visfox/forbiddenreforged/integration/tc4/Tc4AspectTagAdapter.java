package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.aspect.ForbiddenMagicAspectCatalog;
import dev.visfox.forbiddenreforged.FmConfig;
import dev.visfox.forbiddenreforged.FmItems;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectRegistrar;
import thaumcraft.aspect.AspectRegistry;
import thaumcraft.aspect.AspectSync;

public final class Tc4AspectTagAdapter {
    private static final Set<String> DECLARED = new LinkedHashSet<>();

    private Tc4AspectTagAdapter() {
    }

    public static void register() {
        AspectSync.addRegistrar(registry -> new Table(registry).apply());
    }

    public static List<String> declaredTargets() {
        return List.copyOf(DECLARED);
    }

    public static boolean blockTargetsAvailable() {
        return blockTargets;
    }

    private static volatile boolean blockTargets;

    private static final class Table extends AspectRegistrar {
        Table(AspectRegistry registry) {
            super(registry);
        }

        private boolean registerBlock(String target, AspectList aspects) {
            try {
                block(target, aspects);
                blockTargets = true;
                return true;
            } catch (NoSuchMethodError absent) {
                ForbiddenMagic.LOGGER.info(
                    "Block aspect targets are unavailable in this Thaumcraft build; {} keeps no direct aspect", target);
                return false;
            }
        }

        void apply() {
            DECLARED.clear();
            merge("minecraft:netherrack", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 1));
            if (registerBlock("minecraft:nether_portal", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 4))) {
                DECLARED.add("block:minecraft:nether_portal");
            }
            DECLARED.add("#c:ores/quartz");
            register("#c:ores/quartz", tags().add(Aspect.CRYSTAL, 3).add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 2));
            merge("minecraft:nether_star", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 8).add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.SUPERBIA), 8));
            merge("minecraft:nether_wart", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 1));
            merge("minecraft:nether_brick", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 1));
            merge("minecraft:nether_bricks", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 2));
            merge("minecraft:ghast_tear", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.IRA), 4));
            merge("minecraft:wither_skeleton_skull", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 4));
            merge("minecraft:creeper_head", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.IRA), 2));
            merge("minecraft:tnt", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.IRA), 2));
            merge("minecraft:golden_helmet", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.SUPERBIA), 2));
            merge("minecraft:golden_chestplate", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.SUPERBIA), 1));
            merge("minecraft:golden_leggings", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.SUPERBIA), 1));
            merge("minecraft:golden_boots", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.SUPERBIA), 1));
            merge("minecraft:golden_sword", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.SUPERBIA), 2));
            DECLARED.add("#minecraft:beds");
            register("#minecraft:beds", tags().add(Aspect.CRAFT, 3).add(Aspect.CLOTH, 6).add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.DESIDIA), 4));
            DECLARED.add("minecraft:ender_eye");
            register("minecraft:ender_eye", tags().add(Aspect.SENSES, 4).add(Aspect.ELDRITCH, 4).add(Aspect.MAGIC, 3).add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INVIDIA), 4));
            merge("minecraft:cake", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.GULA), 7));
            merge("minecraft:cookie", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.GULA), 1));
            merge("minecraft:fire_charge", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.IRA), 1));
            DECLARED.add("minecraft:comparator");
            register("minecraft:comparator", tags().add(Aspect.MECHANISM, 2).add(Aspect.ORDER, 2).add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INVIDIA), 2));
            if (!FmConfig.lustDisabled()) {
                merge("minecraft:saddle", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.LUXURIA), 2));
                DECLARED.add("minecraft:lead");
                register("minecraft:lead", tags().add(Aspect.BEAST, 2).add(Aspect.CLOTH, 2).add(Aspect.SLIME, 1).add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.LUXURIA), 2));
            }
            addShard("wrath", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 1).add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.IRA), 2).add(Aspect.CRYSTAL, 1));
            addShard("envy", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 1).add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INVIDIA), 2).add(Aspect.CRYSTAL, 1));
            addShard("taint", tags().add(Aspect.TAINT, 3).add(Aspect.CRYSTAL, 1));
            addShard("pride", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 1).add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.SUPERBIA), 2).add(Aspect.CRYSTAL, 1));
            if (!FmConfig.lustDisabled()) {
                addShard("lust", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 1).add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.LUXURIA), 2).add(Aspect.CRYSTAL, 1));
            } else {
                addShard("lust", tags().add(Aspect.VOID, 1));
            }
            addShard("sloth", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 1).add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.DESIDIA), 2).add(Aspect.CRYSTAL, 1));
            addShard("greed", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 1).add(Aspect.GREED, 2).add(Aspect.CRYSTAL, 1));

            register("forbiddenmagic:emerald_fragment", tags().add(Aspect.CRYSTAL, 1));
            register("forbiddenmagic:black_dye", tags().add(Aspect.DARKNESS, 1));
            AspectList blackFlower = tags().add(Aspect.PLANT, 1).add(Aspect.LIFE, 1).add(Aspect.SENSES, 1).add(Aspect.DARKNESS, 1);
            register("forbiddenmagic:umbral_rose", blackFlower);
            register("forbiddenmagic:umbral_rose_bush", blackFlower);
            if (!FmConfig.lustDisabled()) {
                register("forbiddenmagic:riding_crop", tags().add(Aspect.TREE, 2).add(Aspect.CLOTH, 2)
                    .add(Aspect.BEAST, 1).add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.LUXURIA), 1).add(Aspect.WEAPON, 1));
            }
            register("forbiddenmagic:taint_fruit", tags().add(Aspect.TAINT, 2).add(Aspect.POISON, 1));
            register("forbiddenmagic:taint_coal", tags().add(Aspect.TAINT, 2).add(Aspect.ENTROPY, 2));
            register("forbiddenmagic:taint_leaves", tags().add(Aspect.TAINT, 1).add(Aspect.PLANT, 1));
            register("forbiddenmagic:taint_log", tags().add(Aspect.TAINT, 1).add(Aspect.TREE, 3));
            register("forbiddenmagic:taint_planks", tags().add(Aspect.TREE, 1));
            register("forbiddenmagic:taint_stone_brick", tags().add(Aspect.EARTH, 1));

            String gluttony = FmItems.GLUTTONY_SHARD.getId().toString();
            DECLARED.add(gluttony);
            register(gluttony, tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 1).add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.GULA), 2).add(Aspect.CRYSTAL, 1));
            addEntity("thaumcraft:brainy_zombie", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.IRA), 3));
            addEntity("thaumcraft:giant_brainy_zombie", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.IRA), 4));
            addEntity("thaumcraft:firebat", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.IRA), 1).add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 2));
            addEntity("minecraft:wither_skeleton", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 3));
            addEntity("minecraft:creeper", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.IRA), 2));
            addEntity("minecraft:creeper", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.IRA), 4), "powered", 1);
            addEntity("minecraft:wither", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 7).add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.IRA), 7));
            addEntity("minecraft:ocelot", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.DESIDIA), 3));
            addEntity("minecraft:enderman", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.SUPERBIA), 4).add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INVIDIA), 4));
            addEntity("minecraft:ghast", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 3).add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.IRA), 3));
            addEntity("minecraft:zombified_piglin", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.IRA), 6));
            addEntity("minecraft:ender_dragon", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.SUPERBIA), 10));
            if (!FmConfig.lustDisabled()) {
                addEntity("thaumcraft:taintacle", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.LUXURIA), 3));
            }
            addEntity("minecraft:pig", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.GULA), 3));
            addEntity("minecraft:magma_cube", tags().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 1));
        }

        private void merge(String target, AspectList additions) {
            DECLARED.add(target);
            AspectList merged = copy(target);
            for (Aspect aspect : additions.getAspects()) {
                merged.add(aspect, additions.getAmount(aspect));
            }
            register(target, merged);
        }

        private void addShard(String vice, AspectList aspects) {
            String target = FmItems.DEADLY_SHARDS.get(vice).getId().toString();
            DECLARED.add(target);
            register(target, aspects);
        }

        private void addEntity(String target, AspectList additions, Object... nbt) {
            DECLARED.add("entity:" + target);
            entityCopy(target, additions, nbt);
        }
    }
}
