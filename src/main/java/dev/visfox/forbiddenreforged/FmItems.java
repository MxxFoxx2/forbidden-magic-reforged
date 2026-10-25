package dev.visfox.forbiddenreforged;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.item.gear.TcMaterials;
import thaumcraft.platform.Registration;
import thaumcraft.wand.focus.FocusItem;

public final class FmItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ForbiddenMagic.MODID);
    public static final Map<String, DeferredItem<Item>> DEADLY_SHARDS = new LinkedHashMap<>();

    public static final DeferredItem<Item> GLUTTONY_SHARD;
    public static final DeferredItem<Item> SKULL_AXE;
    public static final DeferredItem<Item> EMERALD_FRAGMENT;
    public static final DeferredItem<Item> MORPH_PICKAXE;
    public static final DeferredItem<Item> MORPH_SWORD;
    public static final DeferredItem<Item> MORPH_SHOVEL;
    public static final DeferredItem<Item> MORPH_AXE;
    public static final DeferredItem<MobCrystalItem> MOB_CRYSTAL;
    public static final DeferredItem<DiabolistForkItem> DIABOLIST_FORK;
    public static final DeferredItem<Item> TAINT_COAL;
    public static final DeferredItem<Item> RIDING_CROP;
    public static final DeferredItem<Item> BLACK_DYE;
    public static final DeferredItem<Item> TAINT_FRUIT;
    public static final DeferredItem<Item> RING_NUTRITION;
    public static final DeferredItem<Item> ALCHEMICAL_CAP;
    public static final DeferredItem<Item> EQUIVALENT_ROD;
    public static final DeferredItem<Item> TAINTED_ROD;
    public static final DeferredItem<Item> INFERNAL_ROD;
    public static final DeferredItem<Item> WITCHWOOD_ROD;
    public static final DeferredItem<Item> PROFANE_ROD;
    public static final DeferredItem<Item> WITCHWOOD_STAFF_ROD;
    public static final DeferredItem<Item> TAINT_SHOVEL;
    public static final DeferredItem<Item> TAINT_PICKAXE;
    public static final DeferredItem<CrystalwellItem> CRYSTALWELL;
    public static final DeferredItem<PrimewellItem> PRIMEWELL;
    public static final DeferredItem<Item> BLINK_FOCUS;
    public static final DeferredItem<Item> SUB_COLLAR;

    static {
        for (String vice : new String[]{"wrath", "envy", "taint", "pride", "lust", "sloth", "greed"}) {
            DEADLY_SHARDS.put(vice, vice.equals("lust")
                ? ITEMS.registerItem("lust_shard", LustShardItem::new)
                : ITEMS.registerSimpleItem(vice + "_shard"));
        }
        GLUTTONY_SHARD = ITEMS.register("gluttony_shard",
            () -> new Item(Registration.food(new Item.Properties(), 2, 0.1F)));
        SKULL_AXE = register("skull_axe", SkullAxeItem::new,
            properties -> Registration.sword(properties.rarity(Rarity.UNCOMMON), TcMaterials.ELEMENTAL_TOOL, 3.0F, -2.4F)
                );
        EMERALD_FRAGMENT = ITEMS.registerSimpleItem("emerald_fragment");
        MORPH_PICKAXE = register("morph_pickaxe", MorphToolItem::new,
            properties -> Registration.pickaxe(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 2.0F, -2.8F));
        MORPH_SWORD = register("morph_sword", MorphToolItem::new,
            properties -> Registration.sword(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 3.0F, -2.4F));
        MORPH_SHOVEL = register("morph_shovel", MorphToolItem::new,
            properties -> Registration.shovel(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 1.0F, -3.0F));
        MORPH_AXE = register("morph_axe", MorphToolItem::new,
            properties -> Registration.axe(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 3.0F, -3.0F));
        MOB_CRYSTAL = register("mob_crystal", MobCrystalItem::new,
            properties -> properties.stacksTo(1).rarity(Rarity.UNCOMMON));
        DIABOLIST_FORK = register("diabolist_fork", DiabolistForkItem::new,
            properties -> Registration.sword(properties.rarity(Rarity.UNCOMMON), TcMaterials.THAUMIUM_TOOL, 3.0F, -2.4F)
                );
        TAINT_COAL = ITEMS.registerSimpleItem("taint_coal");
        RIDING_CROP = register("riding_crop", RidingCropItem::new,
            properties -> properties.durability(Tiers.WOOD.getUses())
                .attributes(SwordItem.createAttributes(Tiers.WOOD, 3.0F, -2.4F))
                .component(DataComponents.TOOL, SwordItem.createToolProperties())
                );
        BLACK_DYE = ITEMS.registerItem("black_dye", BlackDyeItem::new);
        TAINT_FRUIT = ITEMS.registerItem("taint_fruit", TaintFruitItem::new);
        RING_NUTRITION = register("ring_nutrition", RingNutritionItem::new,
            properties -> properties.stacksTo(1));
        ALCHEMICAL_CAP = ITEMS.registerSimpleItem("alchemical_cap");
        EQUIVALENT_ROD = ITEMS.registerSimpleItem("equivalent_rod");
        TAINTED_ROD = ITEMS.registerSimpleItem("tainted_rod");
        INFERNAL_ROD = ITEMS.registerSimpleItem("infernal_rod");
        WITCHWOOD_ROD = ITEMS.registerSimpleItem("witchwood_rod");
        WITCHWOOD_STAFF_ROD = ITEMS.registerSimpleItem("witchwood_staff_rod");
        PROFANE_ROD = ITEMS.registerSimpleItem("profane_rod");
        TAINT_SHOVEL = register("taint_shovel", TaintShovelItem::new,
            properties -> Registration.shovel(properties.rarity(Rarity.UNCOMMON), TcMaterials.ELEMENTAL_TOOL, 1.0F, -3.0F)
                );
        TAINT_PICKAXE = register("taint_pickaxe", Item::new,
            properties -> Registration.pickaxe(properties.rarity(Rarity.UNCOMMON), TcMaterials.ELEMENTAL_TOOL, 1.0F, -2.8F)
                );
        CRYSTALWELL = register("crystalwell", CrystalwellItem::new, properties -> properties.stacksTo(1));
        PRIMEWELL = register("primewell", PrimewellItem::new,
            properties -> properties.stacksTo(1).rarity(Rarity.EPIC));
        BLINK_FOCUS = register("blink_focus", BlinkFocusItem::new, FocusItem::properties);
        SUB_COLLAR = FmConfig.lustDisabled() ? null
            : register("sub_collar", SubCollarItem::new, properties -> properties.stacksTo(1).rarity(Rarity.RARE));
    }

    private FmItems() {
    }

    private static <T extends Item> DeferredItem<T> register(
        String name,
        Function<Item.Properties, ? extends T> factory,
        UnaryOperator<Item.Properties> properties
    ) {
        return ITEMS.register(name, () -> factory.apply(properties.apply(new Item.Properties())));
    }

    public static ResourceLocation id(String vice) {
        return ResourceLocation.fromNamespaceAndPath(ForbiddenMagic.MODID, vice + "_shard");
    }
}
