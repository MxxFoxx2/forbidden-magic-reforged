package dev.visfox.forbiddenreforged;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FmItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ForbiddenMagic.MODID);
    public static final Map<String, DeferredItem<Item>> DEADLY_SHARDS = new LinkedHashMap<>();
    public static final DeferredItem<Item> GLUTTONY_SHARD;

    static {
        for (String vice : List.of("wrath", "envy", "taint", "pride", "lust", "sloth", "greed")) {
            DEADLY_SHARDS.put(vice, ITEMS.registerSimpleItem(vice + "_shard"));
        }
        GLUTTONY_SHARD = ITEMS.registerItem("gluttony_shard", Item::new,
            properties -> properties.food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build()));
    }

    private FmItems() {
    }
}
