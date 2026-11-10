package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.common.FmIds;
import dev.visfox.forbiddenreforged.common.WrathCageEssentiaStore;
import dev.visfox.forbiddenreforged.common.WrathCageBlockEntity;
import java.util.function.Consumer;
import dev.visfox.forbiddenreforged.test.EntityGameTests;
import dev.visfox.forbiddenreforged.test.FmGameTestBootstrap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

@EventBusSubscriber(modid = FmIds.MODID)
public final class WrathCageEssentiaGameTests {
    private static final String[] TESTS = {
        "wrath_cage_essentia_capacity_gate_matches_original",
        "wrath_cage_fuel_channel_priority_matches_original",
        "wrath_cage_spawn_bounds_match_original"
    };

    public static void registerFunctions(net.minecraft.core.Registry<java.util.function.Consumer<net.minecraft.gametest.framework.GameTestHelper>> registry) {
        FmGameTestBootstrap.register(registry, "wrath_cage_essentia_capacity_gate_matches_original", WrathCageEssentiaGameTests::capacityGateMatchesOriginal);
        FmGameTestBootstrap.register(registry, "wrath_cage_fuel_channel_priority_matches_original", WrathCageEssentiaGameTests::fuelChannelPriorityMatchesOriginal);
        FmGameTestBootstrap.register(registry, "wrath_cage_spawn_bounds_match_original", WrathCageEssentiaGameTests::spawnBoundsMatchOriginal);
    }

    private WrathCageEssentiaGameTests() {
    }

    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
            Identifier.fromNamespaceAndPath(FmIds.MODID, "fm_wrath_cage_essentia"),
            new TestEnvironmentDefinition.AllOf()
        );
        for (String name : TESTS) {
            ResourceKey<Consumer<GameTestHelper>> function = ResourceKey.create(
                Registries.TEST_FUNCTION,
                Identifier.fromNamespaceAndPath(FmIds.MODID, name)
            );
            event.registerTest(
                Identifier.fromNamespaceAndPath(FmIds.MODID, name),
                new FunctionGameTestInstance(
                    function,
                    new TestData<>(environment, Identifier.parse("forbiddenmagic:test/platform"), 100, 0, true)
                )
            );
        }
    }

    private static void capacityGateMatchesOriginal(GameTestHelper helper) {
        WrathCageEssentiaStore store = new WrathCageEssentiaStore();
        helper.assertTrue(store.hasRoom(), "an empty cage can draw essentia");
        store.load(64, 0, 0, 0);
        helper.assertFalse(store.hasRoom(), "a full mob channel stops all drawing");
        store.load(0, 64, 0, 0);
        helper.assertFalse(store.hasRoom(), "a full wrath channel stops all drawing");
        store.load(0, 0, 64, 0);
        helper.assertFalse(store.hasRoom(), "a full sloth channel stops all drawing");
        store.load(63, 63, 63, 0);
        helper.assertTrue(store.hasRoom(), "drawing resumes when all three channels have room");
        helper.succeed();
    }

    private static void fuelChannelPriorityMatchesOriginal(GameTestHelper helper) {
        WrathCageEssentiaStore store = new WrathCageEssentiaStore();
        store.load(5, 5, 5, 0);
        helper.assertValueEqual(store.consumeFuel(5), WrathCageEssentiaStore.FuelChannel.MOB,
            "mob essentia is consumed before wrath and sloth");
        helper.assertValueEqual(store.consumeFuel(5), WrathCageEssentiaStore.FuelChannel.WRATH,
            "wrath is consumed before sloth");
        helper.assertValueEqual(store.consumeFuel(5), WrathCageEssentiaStore.FuelChannel.SLOTH,
            "sloth is the final fallback");
        helper.assertTrue(store.consumeFuel(5) == null, "fuel is unavailable after all channels are consumed");
        helper.succeed();
    }

    private static void spawnBoundsMatchOriginal(GameTestHelper helper) {
        var bounds = WrathCageBlockEntity.nearbyEntityBounds(new net.minecraft.core.BlockPos(10, 20, 30));
        helper.assertValueEqual(bounds.minX, 2.0, "spawn cap checks eight blocks west");
        helper.assertValueEqual(bounds.minY, 16.0, "spawn cap checks four blocks below");
        helper.assertValueEqual(bounds.minZ, 22.0, "spawn cap checks eight blocks north");
        helper.assertValueEqual(bounds.maxX, 19.0, "spawn cap checks eight blocks east plus block width");
        helper.assertValueEqual(bounds.maxY, 25.0, "spawn cap checks four blocks above plus block height");
        helper.assertValueEqual(bounds.maxZ, 39.0, "spawn cap checks eight blocks south plus block width");
        helper.succeed();
    }
}
