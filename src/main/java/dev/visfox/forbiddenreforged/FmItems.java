package dev.visfox.forbiddenreforged;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;
import thaumcraft.item.gear.TcMaterials;
import thaumcraft.platform.Registration;
import thaumcraft.registry.ModItems;
import thaumcraft.wand.focus.FocusItem;

public final class FmItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ForbiddenMagic.MODID);
    public static final Map<String, DeferredItem<Item>> DEADLY_SHARDS = new LinkedHashMap<>();
    public static final DeferredItem<Item> GLUTTONY_SHARD;
    public static final DeferredItem<Item> SKULL_AXE;
    public static final DeferredItem<Item> EMERALD_FRAGMENT;
    public static final DeferredItem<Item> TAINTED_BLOOD_SHARD;
    public static final DeferredItem<Item> MORPH_PICKAXE;
    public static final DeferredItem<Item> MORPH_SWORD;
    public static final DeferredItem<Item> MORPH_SHOVEL;
    public static final DeferredItem<Item> MORPH_AXE;
    public static final DeferredItem<Item> MOB_CRYSTAL;
    public static final DeferredItem<Item> DIABOLIST_FORK;
    public static final DeferredItem<Item> TAINT_COAL;
    public static final DeferredItem<Item> RIDING_CROP;
    public static final DeferredItem<Item> BLACK_DYE;
    public static final DeferredItem<Item> TAINT_FRUIT;
    public static final DeferredItem<Item> RING_NUTRITION;
    public static final DeferredItem<Item> ALCHEMICAL_CAP;
    public static final DeferredItem<Item> VINTEUM_CAP;
    public static final DeferredItem<Item> TERRASTEEL_CAP;
    public static final DeferredItem<Item> MANASTEEL_CAP;
    public static final DeferredItem<Item> MANASTEEL_INERT_CAP;
    public static final DeferredItem<Item> ELEMENTIUM_CAP;
    public static final DeferredItem<Item> ELEMENTIUM_INERT_CAP;
    public static final DeferredItem<Item> EQUIVALENT_ROD;
    public static final DeferredItem<Item> TAINTED_ROD;
    public static final DeferredItem<Item> INFERNAL_ROD;
    public static final @Nullable DeferredItem<Item> WITCHWOOD_ROD;
    public static final DeferredItem<Item> PROFANE_ROD;
    public static final @Nullable DeferredItem<Item> WITCHWOOD_STAFF_ROD;
    public static final DeferredItem<Item> TAINT_SHOVEL;
    public static final DeferredItem<Item> TAINT_PICKAXE;
    public static final DeferredItem<Item> CRYSTALWELL;
    public static final DeferredItem<Item> PRIMEWELL;
    public static final DeferredItem<Item> BLINK_FOCUS;
    public static final @Nullable DeferredItem<Item> SUB_COLLAR;

    static {
        for (String vice : new String[]{"wrath", "envy", "taint", "pride", "lust", "sloth", "greed"}) {
            DEADLY_SHARDS.put(vice, vice.equals("lust")
                ? ITEMS.registerItem("lust_shard", LustShardItem::new)
                : ITEMS.registerSimpleItem(vice + "_shard"));
        }
        GLUTTONY_SHARD = ITEMS.registerSimpleItem("gluttony_shard", properties -> Registration.food(properties, 2, 0.1F));
        SKULL_AXE = ITEMS.registerItem("skull_axe", SkullAxeItem::new,
            properties -> Registration.sword(properties.rarity(Rarity.UNCOMMON), TcMaterials.ELEMENTAL_TOOL, 3.0F, -2.4F)
                .repairable(ModItems.THAUMIUM_INGOT.get()));
        EMERALD_FRAGMENT = ITEMS.registerSimpleItem("emerald_fragment");
        TAINTED_BLOOD_SHARD = ITEMS.registerSimpleItem("tainted_blood_shard");
        MORPH_PICKAXE = ITEMS.registerItem("morph_pickaxe", MorphToolItem::new,
            properties -> repairable(Registration.pickaxe(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 2.0F, -2.8F)));
        MORPH_SWORD = ITEMS.registerItem("morph_sword", MorphToolItem::new,
            properties -> repairable(Registration.sword(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 3.0F, -2.4F)));
        MORPH_SHOVEL = ITEMS.registerItem("morph_shovel", MorphToolItem::new,
            properties -> repairable(Registration.shovel(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 1.0F, -3.0F)));
        MORPH_AXE = ITEMS.registerItem("morph_axe", MorphToolItem::new,
            properties -> repairable(Registration.axe(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 3.0F, -3.0F)));
        MOB_CRYSTAL = ITEMS.registerItem("mob_crystal", MobCrystalItem::new,
            properties -> properties.stacksTo(1).rarity(Rarity.UNCOMMON));
        DIABOLIST_FORK = ITEMS.registerItem("diabolist_fork", DiabolistForkItem::new,
            properties -> Registration.sword(properties.rarity(Rarity.UNCOMMON), TcMaterials.THAUMIUM_TOOL, 3.0F, -2.4F)
                .repairable(net.minecraft.world.item.Items.QUARTZ));
        TAINT_COAL = ITEMS.registerItem("taint_coal", TaintCoalItem::new);
        RIDING_CROP = ITEMS.registerItem("riding_crop", RidingCropItem::new,
            properties -> properties.sword(ToolMaterial.WOOD, 3.0F, -2.4F)
                .repairable(TagKey.create(Registries.ITEM, ForbiddenMagic.id("riding_crop_repair"))));
        BLACK_DYE = ITEMS.registerItem("black_dye", BlackDyeItem::new,
            properties -> properties.stacksTo(64));
        TAINT_FRUIT = ITEMS.registerItem("taint_fruit", TaintFruitItem::new,
            properties -> properties.stacksTo(64));
        RING_NUTRITION = ITEMS.registerItem("ring_nutrition", RingNutritionItem::new,
            properties -> properties.stacksTo(1));
        ALCHEMICAL_CAP = ITEMS.registerSimpleItem("alchemical_cap");
        VINTEUM_CAP = ITEMS.registerSimpleItem("vinteum_cap");
        TERRASTEEL_CAP = ITEMS.registerSimpleItem("terrasteel_cap");
        MANASTEEL_CAP = ITEMS.registerSimpleItem("manasteel_cap");
        MANASTEEL_INERT_CAP = ITEMS.registerSimpleItem("manasteel_inert_cap");
        ELEMENTIUM_CAP = ITEMS.registerSimpleItem("elementium_cap");
        ELEMENTIUM_INERT_CAP = ITEMS.registerSimpleItem("elementium_inert_cap");
        EQUIVALENT_ROD = ITEMS.registerSimpleItem("equivalent_rod");
        TAINTED_ROD = ITEMS.registerSimpleItem("tainted_rod");
        INFERNAL_ROD = ITEMS.registerSimpleItem("infernal_rod");
        if (ModList.get().isLoaded("arsmagica2")) {
            WITCHWOOD_ROD = ITEMS.registerSimpleItem("witchwood_rod");
            WITCHWOOD_STAFF_ROD = ITEMS.registerSimpleItem("witchwood_staff_rod");
        } else {
            WITCHWOOD_ROD = null;
            WITCHWOOD_STAFF_ROD = null;
        }
        PROFANE_ROD = ITEMS.registerSimpleItem("profane_rod");
        TAINT_SHOVEL = ITEMS.registerItem("taint_shovel", TaintShovelItem::new,
            properties -> Registration.shovel(properties.rarity(Rarity.UNCOMMON), TcMaterials.ELEMENTAL_TOOL, 1.0F, -3.0F)
                .repairable(ModItems.THAUMIUM_INGOT.get()));
        TAINT_PICKAXE = ITEMS.registerItem("taint_pickaxe", Item::new,
            properties -> Registration.pickaxe(properties.rarity(Rarity.UNCOMMON), TcMaterials.ELEMENTAL_TOOL, 1.0F, -2.8F)
                .repairable(ModItems.THAUMIUM_INGOT.get()));
        CRYSTALWELL = ITEMS.registerItem("crystalwell", CrystalwellItem::new, properties -> properties.stacksTo(1));
        PRIMEWELL = ITEMS.registerItem("primewell", PrimewellItem::new, properties -> properties.stacksTo(1).rarity(Rarity.EPIC));
        BLINK_FOCUS = ITEMS.registerItem("blink_focus", BlinkFocusItem::new,
            properties -> FocusItem.properties(properties));
        SUB_COLLAR = FmConfig.lustDisabled() ? null
            : ITEMS.registerItem("sub_collar", SubCollarItem::new, properties -> properties.stacksTo(1).rarity(Rarity.RARE));
    }

    private FmItems() {
    }

    private static Item.Properties repairable(Item.Properties properties) {
        return properties.repairable(DEADLY_SHARDS.get("envy").get());
    }

    public static Identifier id(String vice) {
        return Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, vice + "_shard");
    }
}
