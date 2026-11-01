package dev.visfox.forbiddenreforged.integration.tc4;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import thaumcraft.item.gear.TcMaterials;
import thaumcraft.platform.Registration;
import thaumcraft.registry.ModItems;

public final class Tc4ItemRegistrationAdapter {
    private Tc4ItemRegistrationAdapter() {
    }

    public static Item.Properties gluttonyFood(Item.Properties properties) {
        return Registration.food(properties, 2, 0.1F);
    }

    public static Item.Properties skullAxe(Item.Properties properties) {
        return Registration.sword(properties.rarity(Rarity.UNCOMMON), TcMaterials.ELEMENTAL_TOOL, 3.0F, -2.4F)
            .repairable(ModItems.THAUMIUM_INGOT.get());
    }

    public static Item.Properties morphPickaxe(Item.Properties properties) {
        return Registration.pickaxe(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 2.0F, -2.8F);
    }

    public static Item.Properties morphSword(Item.Properties properties) {
        return Registration.sword(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 3.0F, -2.4F);
    }

    public static Item.Properties morphShovel(Item.Properties properties) {
        return Registration.shovel(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 1.0F, -3.0F);
    }

    public static Item.Properties morphAxe(Item.Properties properties) {
        return Registration.axe(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 3.0F, -3.0F);
    }

    public static Item.Properties diabolistFork(Item.Properties properties) {
        return Registration.sword(properties.rarity(Rarity.UNCOMMON), TcMaterials.THAUMIUM_TOOL, 3.0F, -2.4F);
    }

    public static Item.Properties taintShovel(Item.Properties properties) {
        return Registration.shovel(properties.rarity(Rarity.UNCOMMON), TcMaterials.ELEMENTAL_TOOL, 1.0F, -3.0F)
            .repairable(ModItems.THAUMIUM_INGOT.get());
    }

    public static Item.Properties taintPickaxe(Item.Properties properties) {
        return Registration.pickaxe(properties.rarity(Rarity.UNCOMMON), TcMaterials.ELEMENTAL_TOOL, 1.0F, -2.8F)
            .repairable(ModItems.THAUMIUM_INGOT.get());
    }
}
