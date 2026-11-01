package dev.visfox.forbiddenreforged.integration.runtime;

import net.minecraft.world.item.Item;

/** Host-specific Item.Properties customization for shared Forbidden Magic items. */
public interface HostItemPropertyServices {
    Item.Properties gluttonyFoodProperties(Item.Properties properties);

    Item.Properties skullAxeProperties(Item.Properties properties);

    Item.Properties morphPickaxeProperties(Item.Properties properties);

    Item.Properties morphSwordProperties(Item.Properties properties);

    Item.Properties morphShovelProperties(Item.Properties properties);

    Item.Properties morphAxeProperties(Item.Properties properties);

    Item.Properties diabolistForkProperties(Item.Properties properties);

    Item.Properties taintShovelProperties(Item.Properties properties);

    Item.Properties taintPickaxeProperties(Item.Properties properties);

    Item.Properties blinkFocusProperties(Item.Properties properties);
}
