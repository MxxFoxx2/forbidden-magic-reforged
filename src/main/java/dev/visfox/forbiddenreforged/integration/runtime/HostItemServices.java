package dev.visfox.forbiddenreforged.integration.runtime;

import net.minecraft.world.item.Item;

/**
 * Host-provided item implementations and host-specific item properties.
 *
 * All factories return Minecraft Item values; concrete host item classes stay
 * inside the selected integration implementation.
 */
public interface HostItemServices {
    Item.Properties gluttonyFoodProperties(Item.Properties properties);

    Item.Properties skullAxeProperties(Item.Properties properties);

    Item.Properties morphPickaxeProperties(Item.Properties properties);

    Item.Properties morphSwordProperties(Item.Properties properties);

    Item.Properties morphShovelProperties(Item.Properties properties);

    Item.Properties morphAxeProperties(Item.Properties properties);

    Item.Properties diabolistForkProperties(Item.Properties properties);

    Item.Properties taintShovelProperties(Item.Properties properties);

    Item.Properties taintPickaxeProperties(Item.Properties properties);

    Item createMorphTool(Item.Properties properties);

    Item createRingNutrition(Item.Properties properties);

    Item createCrystalwell(Item.Properties properties);

    Item createPrimewell(Item.Properties properties);

    Item createBlinkFocus(Item.Properties properties);

    Item.Properties blinkFocusProperties(Item.Properties properties);

    Item primordialPearlItem();

    Item createSubCollar(Item.Properties properties);
}
