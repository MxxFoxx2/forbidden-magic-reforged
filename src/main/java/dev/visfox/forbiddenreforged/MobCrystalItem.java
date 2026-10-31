package dev.visfox.forbiddenreforged;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;

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
            Component.translatable(entityTranslationKey(ResourceLocation.parse(id))));
    }

    private static String entityTranslationKey(ResourceLocation id) {
        return "entity." + id.getNamespace() + "." + id.getPath().replace('/', '.');
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Aspect aspect = FmSpawnerMobs.aspect(mob(stack));
        if (aspect != null) {
            tooltip.add(aspect.getDescription());
        }
    }
}
