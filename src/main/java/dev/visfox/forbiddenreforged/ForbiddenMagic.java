package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.test.AspectGameTests;
import dev.visfox.forbiddenreforged.test.BlockGameTests;
import dev.visfox.forbiddenreforged.test.EnchantmentGameTests;
import dev.visfox.forbiddenreforged.test.EntityGameTests;
import dev.visfox.forbiddenreforged.test.ItemGameTests;
import dev.visfox.forbiddenreforged.test.RecipeGameTests;
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
        FmAspects.init();
        FmAspectTags.register();
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
        AspectGameTests.FUNCTIONS.register(modEventBus);
        ItemGameTests.FUNCTIONS.register(modEventBus);
        RecipeGameTests.FUNCTIONS.register(modEventBus);
        EnchantmentGameTests.FUNCTIONS.register(modEventBus);
        BlockGameTests.FUNCTIONS.register(modEventBus);
        EntityGameTests.FUNCTIONS.register(modEventBus);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
