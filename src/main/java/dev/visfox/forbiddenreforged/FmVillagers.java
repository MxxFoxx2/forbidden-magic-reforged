package dev.visfox.forbiddenreforged;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FmVillagers {
    private static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, ForbiddenMagic.MODID);

    public static final DeferredHolder<VillagerProfession, VillagerProfession> HERETIC = profession("heretic");

    private FmVillagers() {
    }

    private static DeferredHolder<VillagerProfession, VillagerProfession> profession(String name) {
        return PROFESSIONS.register(name, () -> {
            String tradeSetName = name;
            if (name.equals("heretic") && FmConfig.lustDisabled()) {
                tradeSetName = "heretic_no_lust";
            }
            Int2ObjectMap<ResourceKey<TradeSet>> tradeSets = new Int2ObjectOpenHashMap<>();
            for (int level = 1; level <= 5; level++) {
                tradeSets.put(level, ResourceKey.create(Registries.TRADE_SET, ForbiddenMagic.id(tradeSetName + "/level_" + level)));
            }
            return new VillagerProfession(
                Component.translatable("entity.forbiddenmagic.villager." + name),
                holder -> false,
                holder -> false,
                ImmutableSet.of(),
                ImmutableSet.of(),
                SoundEvents.VILLAGER_WORK_CLERIC,
                tradeSets
            );
        });
    }

    public static void register(IEventBus bus) {
        PROFESSIONS.register(bus);
    }
}
