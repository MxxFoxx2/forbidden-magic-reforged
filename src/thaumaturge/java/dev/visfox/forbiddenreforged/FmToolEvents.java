package dev.visfox.forbiddenreforged;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

public final class FmToolEvents {
    private FmToolEvents() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(FmToolEvents::onFoodFinished);
    }

    public static boolean hasNutritionRing(Player player) {
        return RingNutritionItem.worn(player);
    }

    private static void onFoodFinished(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        ItemStack consumed = event.getItem();
        if (consumed.get(DataComponents.FOOD) == null || !hasNutritionRing(player)) {
            return;
        }
        player.getFoodData().eat(2, 2.0F);
    }
}
