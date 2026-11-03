package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import org.jetbrains.annotations.Nullable;
import net.neoforged.fml.ModList;
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
    public static final DeferredItem<MobCrystalItem> MOB_CRYSTAL;
    public static final DeferredItem<DiabolistForkItem> DIABOLIST_FORK;
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
        var properties = HostAdapterRegistry.requireItemProperties();
        var factories = HostAdapterRegistry.requireItemFactories();

        for (String vice : new String[]{"wrath", "envy", "taint", "pride", "lust", "sloth", "greed"}) {
            DEADLY_SHARDS.put(vice, vice.equals("lust")
                ? ITEMS.registerItem("lust_shard", LustShardItem::new)
                : ITEMS.registerSimpleItem(vice + "_shard"));
        }
        GLUTTONY_SHARD = ITEMS.register("gluttony_shard",
            () -> new Item(properties.gluttonyFoodProperties(new Item.Properties())));
        SKULL_AXE = register("skull_axe", SkullAxeItem::new, properties::skullAxeProperties);
        EMERALD_FRAGMENT = ITEMS.registerSimpleItem("emerald_fragment");
        TAINTED_BLOOD_SHARD = ITEMS.registerSimpleItem("tainted_blood_shard");
        MORPH_PICKAXE = register("morph_pickaxe", factories::createMorphTool, properties::morphPickaxeProperties);
        MORPH_SWORD = register("morph_sword", factories::createMorphTool, properties::morphSwordProperties);
        MORPH_SHOVEL = register("morph_shovel", factories::createMorphTool, properties::morphShovelProperties);
        MORPH_AXE = register("morph_axe", factories::createMorphTool, properties::morphAxeProperties);
        MOB_CRYSTAL = register("mob_crystal", MobCrystalItem::new,
            itemProperties -> itemProperties.stacksTo(1).rarity(Rarity.UNCOMMON));
        DIABOLIST_FORK = register("diabolist_fork", DiabolistForkItem::new, properties::diabolistForkProperties);
        TAINT_COAL = ITEMS.registerSimpleItem("taint_coal");
        RIDING_CROP = register("riding_crop", RidingCropItem::new,
            itemProperties -> itemProperties.durability(Tiers.WOOD.getUses())
                .attributes(SwordItem.createAttributes(Tiers.WOOD, 3.0F, -2.4F))
                .component(DataComponents.TOOL, SwordItem.createToolProperties()));
        BLACK_DYE = ITEMS.registerItem("black_dye", BlackDyeItem::new);
        TAINT_FRUIT = ITEMS.registerItem("taint_fruit", TaintFruitItem::new);
        RING_NUTRITION = register("ring_nutrition", factories::createRingNutrition,
            itemProperties -> itemProperties.stacksTo(1));
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
        TAINT_SHOVEL = register("taint_shovel", TaintShovelItem::new, properties::taintShovelProperties);
        TAINT_PICKAXE = register("taint_pickaxe", Item::new, properties::taintPickaxeProperties);
        CRYSTALWELL = register("crystalwell", factories::createCrystalwell,
            itemProperties -> itemProperties.stacksTo(1));
        PRIMEWELL = register("primewell", factories::createPrimewell,
            itemProperties -> itemProperties.stacksTo(1).rarity(Rarity.EPIC));
        BLINK_FOCUS = register("blink_focus", factories::createBlinkFocus, properties::blinkFocusProperties);
        SUB_COLLAR = FmConfig.lustDisabled() ? null
            : register("sub_collar", factories::createSubCollar,
                itemProperties -> itemProperties.stacksTo(1).rarity(Rarity.RARE));
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
