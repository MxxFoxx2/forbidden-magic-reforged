package dev.visfox.forbiddenreforged.integration.runtime;

import net.minecraft.world.item.Item;

/** Host-backed Item implementations exposed as neutral Minecraft Item values. */
public interface HostItemFactoryServices {
    Item createMorphTool(Item.Properties properties);

    Item createRingNutrition(Item.Properties properties);

    Item createCrystalwell(Item.Properties properties);

    Item createPrimewell(Item.Properties properties);

    Item createBlinkFocus(Item.Properties properties);

    Item primordialPearlItem();

    Item createSubCollar(Item.Properties properties);
}
