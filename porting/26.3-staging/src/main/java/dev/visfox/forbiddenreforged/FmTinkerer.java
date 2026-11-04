package dev.visfox.forbiddenreforged;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.fml.ModList;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;

public final class FmTinkerer {
    private static int registered;

    private FmTinkerer() {
    }

    public static void register() {
        if (!FmConfig.enchantmentsEnabled() || !ModList.get().isLoaded("thaumictinkerer")) {
            return;
        }
        try {
            Class<?> manager = Class.forName("thaumictinkerer.enchanting.EnchantmentManager");
            Method data = manager.getMethod("data");
            Map<Object, Object> costs = (Map<Object, Object>) data.invoke(null);
            Class<?> datum = Class.forName("thaumictinkerer.enchanting.EnchantmentManager$Data");
            Constructor<?> constructor = datum.getConstructor(ResourceKey.class, net.minecraft.resources.ResourceLocation.class, boolean.class, AspectList.class, String.class);
            add(costs, constructor, FmEnchantments.WRATH, "textures/misc/wrath.png", new AspectList().add(Aspect.ENTROPY, 6).add(Aspect.FIRE, 6), "WRATH");
            add(costs, constructor, FmEnchantments.GREEDY, "textures/misc/greedy.png", new AspectList().add(Aspect.ENTROPY, 50).add(Aspect.EARTH, 30).add(Aspect.ORDER, 20), "GREEDY");
            add(costs, constructor, FmEnchantments.EDUCATIONAL, "textures/misc/educational.png", new AspectList().add(Aspect.AIR, 10).add(Aspect.FIRE, 10).add(Aspect.WATER, 10).add(Aspect.EARTH, 10).add(Aspect.ORDER, 15).add(Aspect.ENTROPY, 15), "EDUCATIONAL");
            add(costs, constructor, FmEnchantments.CONSUMING, "textures/misc/consuming.png", new AspectList().add(Aspect.ENTROPY, 10), "CONSUMING");
            add(costs, constructor, FmEnchantments.CORRUPTING, "textures/misc/corrupting.png", new AspectList().add(Aspect.ENTROPY, 20).add(Aspect.FIRE, 15).add(Aspect.EARTH, 10), "CORRUPTING");
            add(costs, constructor, FmEnchantments.CLUSTER, "textures/misc/lucrative.png", new AspectList().add(Aspect.FIRE, 5).add(Aspect.EARTH, 5).add(Aspect.ORDER, 5), "CLUSTER");
            add(costs, constructor, FmEnchantments.IMPACT, "textures/misc/impact.png", new AspectList().add(Aspect.ENTROPY, 30).add(Aspect.EARTH, 30), "IMPACT");
            add(costs, constructor, FmEnchantments.VOIDTOUCHED, "textures/misc/voidtouched.png", new AspectList().add(Aspect.ENTROPY, 70), "VOIDTOUCHED");
            registered = 8;
        } catch (ReflectiveOperationException | LinkageError error) {
            ForbiddenMagic.LOGGER.warn("Thaumic Tinkerer is present but its enchantment API is missing", error);
        }
    }

    public static int registeredCount() {
        return registered;
    }

    private static void add(Map<Object, Object> costs, Constructor<?> constructor, ResourceKey<Enchantment> key, String texture, AspectList aspects, String research) throws ReflectiveOperationException {
        Object datum = constructor.newInstance(key, ForbiddenMagic.id(texture), false, aspects, research);
        costs.put(key, datum);
    }
}
