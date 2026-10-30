package dev.visfox.forbiddenreforged;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;

public class MobCrystalItem extends Item {
    public MobCrystalItem(Properties properties) {
        super(properties);
    }

    public static @Nullable String mob(ItemStack stack) {
        return stack.get(FmComponents.MOB_ID.get());
    }

    public static void setMob(ItemStack stack, String id) {
        stack.set(FmComponents.MOB_ID.get(), id);
    }

    @Override
    public Component getName(ItemStack stack) {
        String id = mob(stack);
        if (id == null) {
            return Component.translatable("item.forbiddenmagic.mob_crystal.empty");
        }
        return Component.translatable("item.forbiddenmagic.mob_crystal.filled",
            Component.translatable(Util.makeDescriptionId("entity", net.minecraft.resources.Identifier.parse(id))));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        ResourceKey<IAspect> aspect = FmSpawnerMobs.aspect(mob(stack));
        if (aspect != null) {
            tooltip.accept(Component.translatable("aspect." + aspect.identifier().getNamespace() + "." + aspect.identifier().getPath() + ".desc"));
        }
    }
}
