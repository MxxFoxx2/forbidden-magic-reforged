package dev.visfox.forbiddenreforged;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.FuelValues;
import org.jspecify.annotations.Nullable;

public class TaintCoalItem extends Item {
    private static final int MIN_BURN = 1;
    private static final int MAX_BURN = 2400;

    public TaintCoalItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType, FuelValues fuelValues) {
        return ThreadLocalRandom.current().nextInt(MIN_BURN, MAX_BURN + 1);
    }
}
