package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.test.FmGameTests;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ForbiddenMagic.MODID)
public final class ForbiddenMagic {
    public static final String MODID = "forbiddenmagic";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public ForbiddenMagic(IEventBus modEventBus, ModContainer modContainer) {
        FmConfig.register(modContainer);
        FmComponents.register(modEventBus);
        FmBlocks.BLOCKS.register(modEventBus);
        FmBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        FmItems.ITEMS.register(modEventBus);
        FmCreativeTab.TABS.register(modEventBus);
        FmDrops.register();
        FmEnchantmentEffects.register();
        FmGameTests.FUNCTIONS.register(modEventBus);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
