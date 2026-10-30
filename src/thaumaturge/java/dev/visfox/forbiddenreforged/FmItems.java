package dev.visfox.forbiddenreforged;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FmItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ForbiddenMagic.MODID);
    public static final Map<String, DeferredItem<Item>> DEADLY_SHARDS = new LinkedHashMap<>();
    public static final DeferredItem<Item> GLUTTONY_SHARD;
    public static final DeferredItem<Item> EMERALD_FRAGMENT;
    public static final DeferredItem<Item> TAINT_COAL;
    public static final DeferredItem<Item> TAINT_FRUIT;
    public static final DeferredItem<Item> BLACK_DYE;
    public static final DeferredItem<Item> DIABOLIST_FORK;
    public static final DeferredItem<Item> TAINT_SHOVEL;
    public static final DeferredItem<Item> TAINT_PICKAXE;
    public static final DeferredItem<Item> MOB_CRYSTAL;
    public static final DeferredItem<Item> SKULL_AXE;
    public static final DeferredItem<Item> RIDING_CROP;
    public static final DeferredItem<Item> RING_NUTRITION;

    static {
        for (String vice : List.of("wrath", "envy", "taint", "pride", "lust", "sloth", "greed")) {
            DEADLY_SHARDS.put(vice, ITEMS.registerSimpleItem(vice + "_shard"));
        }
        GLUTTONY_SHARD = ITEMS.registerItem("gluttony_shard", Item::new,
            properties -> properties.food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build()));
        EMERALD_FRAGMENT = ITEMS.registerSimpleItem("emerald_fragment");
        TAINT_COAL = ITEMS.registerItem("taint_coal", TaintCoalItem::new);
        TAINT_FRUIT = ITEMS.registerItem("taint_fruit", TaintFruitItem::new, properties -> properties.stacksTo(64));
        BLACK_DYE = ITEMS.registerItem("black_dye", BlackDyeItem::new, properties -> properties.stacksTo(64));
        DIABOLIST_FORK = ITEMS.registerItem("diabolist_fork", DiabolistForkItem::new,
            properties -> properties.sword(FmMaterials.THAUMIUM, 3.0F, -2.4F).rarity(Rarity.UNCOMMON)
                .repairable(Items.QUARTZ));
        TAINT_SHOVEL = ITEMS.registerItem("taint_shovel", TaintShovelItem::new,
            properties -> properties.shovel(FmMaterials.ELEMENTAL, 1.0F, -3.0F).rarity(Rarity.UNCOMMON));
        TAINT_PICKAXE = ITEMS.registerItem("taint_pickaxe", Item::new,
            properties -> properties.pickaxe(FmMaterials.ELEMENTAL, 2.0F, -2.8F).rarity(Rarity.UNCOMMON));
        MOB_CRYSTAL = ITEMS.registerItem("mob_crystal", MobCrystalItem::new,
            properties -> properties.stacksTo(1).rarity(Rarity.UNCOMMON));
        SKULL_AXE = ITEMS.registerItem("skull_axe", SkullAxeItem::new,
            properties -> properties.sword(FmMaterials.ELEMENTAL, 3.0F, -2.4F).rarity(Rarity.UNCOMMON));
        RIDING_CROP = ITEMS.registerItem("riding_crop", RidingCropItem::new,
            properties -> properties.sword(ToolMaterial.WOOD, 3.0F, -2.4F)
                .repairable(TagKey.create(Registries.ITEM, ForbiddenMagic.id("riding_crop_repair"))));
        RING_NUTRITION = ITEMS.registerItem("ring_nutrition", RingNutritionItem::new);
    }

    private FmItems() {
    }
}
