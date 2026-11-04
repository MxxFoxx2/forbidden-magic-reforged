package dev.visfox.forbiddenreforged.test;

import dev.visfox.forbiddenreforged.common.FmIds;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Test-only hook in the gameTest source set: registers every GameTest function
 * into the built-in {@code minecraft:test_function} registry. The production
 * {@code ForbiddenMagic} class never references test code, and these classes are
 * not part of the shipped jar.
 *
 * <p>{@code RegisterEvent} for that registry is dispatched by
 * {@code GameData.postRegisterEvents()} while the registry is still unfrozen,
 * which is the only window where entries can be added. The later
 * {@code RegisterGameTestsEvent} only exposes the test environment and test
 * instance registries, and fires after the registries have been frozen.
 */
@EventBusSubscriber(modid = FmIds.MODID)
public final class FmGameTestBootstrap {
    private FmGameTestBootstrap() {
    }

    @SubscribeEvent
    static void registerTestFunctions(RegisterEvent event) {
        if (!Registries.TEST_FUNCTION.equals(event.getRegistryKey())) {
            return;
        }
        Registry<Consumer<GameTestHelper>> registry = event.getRegistry(Registries.TEST_FUNCTION);
        AspectGameTests.registerFunctions(registry);
        BlockGameTests.registerFunctions(registry);
        EnchantmentGameTests.registerFunctions(registry);
        EntityGameTests.registerFunctions(registry);
        ItemGameTests.registerFunctions(registry);
        RecipeGameTests.registerFunctions(registry);
        dev.visfox.forbiddenreforged.WrathCageEssentiaGameTests.registerFunctions(registry);
    }

    public static void register(Registry<Consumer<GameTestHelper>> registry, String name, Consumer<GameTestHelper> function) {
        Registry.register(registry, Identifier.fromNamespaceAndPath(FmIds.MODID, name), function);
    }
}
