package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.FmEnchantments;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumictinkerer.enchanting.EnchantmentManager;

public final class Tc4TinkererLinkedAdapter {
    private Tc4TinkererLinkedAdapter() {
    }

    public static int register() {
        Map<ResourceKey<Enchantment>, EnchantmentManager.Data> costs = EnchantmentManager.data();
        List<PendingCost> pending = List.of(
            new PendingCost(FmEnchantments.WRATH, "textures/misc/wrath.png",
                new AspectList().add(Aspect.ENTROPY, 6).add(Aspect.FIRE, 6), "WRATH"),
            new PendingCost(FmEnchantments.GREEDY, "textures/misc/greedy.png",
                new AspectList().add(Aspect.ENTROPY, 50).add(Aspect.EARTH, 30).add(Aspect.ORDER, 20), "GREEDY"),
            new PendingCost(FmEnchantments.EDUCATIONAL, "textures/misc/educational.png",
                new AspectList().add(Aspect.AIR, 10).add(Aspect.FIRE, 10).add(Aspect.WATER, 10)
                    .add(Aspect.EARTH, 10).add(Aspect.ORDER, 15).add(Aspect.ENTROPY, 15), "EDUCATIONAL"),
            new PendingCost(FmEnchantments.CONSUMING, "textures/misc/consuming.png",
                new AspectList().add(Aspect.ENTROPY, 10), "CONSUMING"),
            new PendingCost(FmEnchantments.CORRUPTING, "textures/misc/corrupting.png",
                new AspectList().add(Aspect.ENTROPY, 20).add(Aspect.FIRE, 15).add(Aspect.EARTH, 10), "CORRUPTING"),
            new PendingCost(FmEnchantments.CLUSTER, "textures/misc/lucrative.png",
                new AspectList().add(Aspect.FIRE, 5).add(Aspect.EARTH, 5).add(Aspect.ORDER, 5), "CLUSTER"),
            new PendingCost(FmEnchantments.IMPACT, "textures/misc/impact.png",
                new AspectList().add(Aspect.ENTROPY, 30).add(Aspect.EARTH, 30), "IMPACT"),
            new PendingCost(FmEnchantments.VOIDTOUCHED, "textures/misc/voidtouched.png",
                new AspectList().add(Aspect.ENTROPY, 70), "VOIDTOUCHED")
        );

        List<Map.Entry<ResourceKey<Enchantment>, EnchantmentManager.Data>> materialized =
            new ArrayList<>(pending.size());
        for (PendingCost cost : pending) {
            materialized.add(Map.entry(
                cost.key(),
                new EnchantmentManager.Data(
                    cost.key(),
                    ForbiddenMagic.id(cost.texture()),
                    false,
                    cost.aspects(),
                    cost.research()
                )
            ));
        }

        applyAtomically(costs, materialized);
        return materialized.size();
    }

    private static void applyAtomically(
        Map<ResourceKey<Enchantment>, EnchantmentManager.Data> costs,
        List<Map.Entry<ResourceKey<Enchantment>, EnchantmentManager.Data>> materialized
    ) {
        Map<ResourceKey<Enchantment>, EnchantmentManager.Data> previousValues = new HashMap<>();
        Set<ResourceKey<Enchantment>> previouslyPresent = new HashSet<>();
        for (Map.Entry<ResourceKey<Enchantment>, EnchantmentManager.Data> entry : materialized) {
            ResourceKey<Enchantment> key = entry.getKey();
            if (costs.containsKey(key)) {
                previouslyPresent.add(key);
                previousValues.put(key, costs.get(key));
            }
        }

        try {
            for (Map.Entry<ResourceKey<Enchantment>, EnchantmentManager.Data> entry : materialized) {
                costs.put(entry.getKey(), entry.getValue());
            }
        } catch (RuntimeException error) {
            try {
                for (Map.Entry<ResourceKey<Enchantment>, EnchantmentManager.Data> entry : materialized) {
                    ResourceKey<Enchantment> key = entry.getKey();
                    if (previouslyPresent.contains(key)) {
                        costs.put(key, previousValues.get(key));
                    } else {
                        costs.remove(key);
                    }
                }
            } catch (RuntimeException rollbackError) {
                error.addSuppressed(rollbackError);
            }
            throw error;
        }
    }

    private record PendingCost(
        ResourceKey<Enchantment> key,
        String texture,
        AspectList aspects,
        String research
    ) {
    }
}
