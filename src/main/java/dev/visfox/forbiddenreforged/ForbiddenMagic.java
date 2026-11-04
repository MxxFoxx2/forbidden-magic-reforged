package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.common.FmAspects;
import dev.visfox.forbiddenreforged.common.FmBlockEntities;
import dev.visfox.forbiddenreforged.common.FmBlocks;
import dev.visfox.forbiddenreforged.common.FmCommands;
import dev.visfox.forbiddenreforged.common.FmComponents;
import dev.visfox.forbiddenreforged.common.FmConfig;
import dev.visfox.forbiddenreforged.common.FmCreativeTab;
import dev.visfox.forbiddenreforged.common.FmDrops;
import dev.visfox.forbiddenreforged.common.FmEnchantmentEffects;
import dev.visfox.forbiddenreforged.common.FmIds;
import dev.visfox.forbiddenreforged.common.FmItems;
import dev.visfox.forbiddenreforged.common.FmVillagers;
import dev.visfox.forbiddenreforged.common.ScribeWellEvents;
import dev.visfox.forbiddenreforged.common.FmToolEvents;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4HostAdapter;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * Composition root: the only place that selects the concrete host adapter.
 * All shared content lives in {@code common}; GameTests live in their own source set.
 */
@Mod(FmIds.MODID)
public final class ForbiddenMagic {
    public ForbiddenMagic(IEventBus modEventBus, ModContainer modContainer) {
        FmConfig.register(modContainer);
        HostAdapterRegistry.install(Tc4HostAdapter.INSTANCE);
        FmAspects.init();
        HostAdapterRegistry.require().registerAspectTags();
        FmComponents.register(modEventBus);
        FmBlocks.BLOCKS.register(modEventBus);
        FmBlockEntities.register(modEventBus);
        FmItems.ITEMS.register(modEventBus);
        FmCreativeTab.register(modEventBus);
        FmVillagers.register(modEventBus);
        FmDrops.register();
        FmEnchantmentEffects.register();
        FmToolEvents.register();
        ScribeWellEvents.register();
    }
}
