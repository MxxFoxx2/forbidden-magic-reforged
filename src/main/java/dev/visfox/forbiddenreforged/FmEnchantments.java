package dev.visfox.forbiddenreforged;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class FmEnchantments {
    public static final ResourceKey<Enchantment> WRATH = key("wrath");
    public static final ResourceKey<Enchantment> GREEDY = key("greedy");
    public static final ResourceKey<Enchantment> CONSUMING = key("consuming");
    public static final ResourceKey<Enchantment> EDUCATIONAL = key("educational");
    public static final ResourceKey<Enchantment> CORRUPTING = key("corrupting");
    public static final ResourceKey<Enchantment> CLUSTER = key("cluster");
    public static final ResourceKey<Enchantment> IMPACT = key("impact");
    public static final ResourceKey<Enchantment> VOIDTOUCHED = key("voidtouched");

    private FmEnchantments() {
    }

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, ForbiddenMagic.id(name));
    }

    public static int level(RegistryAccess access, ItemStack stack, ResourceKey<Enchantment> key) {
        if (stack.isEmpty()) {
            return 0;
        }
        return access.lookupOrThrow(Registries.ENCHANTMENT)
            .get(key)
            .map(holder -> stack.getEnchantmentLevel(holder))
            .orElse(0);
    }

    public static boolean enabled() {
        return FmConfig.enchantmentsEnabled();
    }
}
