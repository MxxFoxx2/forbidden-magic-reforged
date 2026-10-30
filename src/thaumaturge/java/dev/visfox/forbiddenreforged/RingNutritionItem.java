package dev.visfox.forbiddenreforged;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class RingNutritionItem extends Item {
    public RingNutritionItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static boolean worn(Player player) {
        return CuriosApi.getCuriosInventory(player)
            .flatMap(inventory -> inventory.findFirstCurio(stack -> stack.is(FmItems.RING_NUTRITION.get())))
            .isPresent();
    }

    public static ICurio curio() {
        return new ICurio() {
            @Override
            public ItemStack getStack() {
                return ItemStack.EMPTY;
            }
        };
    }
}
