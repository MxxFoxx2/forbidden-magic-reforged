package dev.visfox.forbiddenreforged.test;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.taint.ITaintBlock;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class FmGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
        DeferredRegister.create(Registries.TEST_FUNCTION, ForbiddenMagic.MODID);

    private static final String[] TESTS = {
        "thaumaturge_host_is_present",
        "sin_aspects_are_registered",
        "shards_are_registered",
        "forbidden_research_entry_and_category_resolve",
        "tainted_blocks_are_registered",
        "tainted_blocks_use_taint_contract",
        "simple_items_are_registered",
        "rose_and_cake_blocks_are_registered"
    };

    static {
        FUNCTIONS.register(TESTS[0], () -> FmGameTests::thaumaturgeHostIsPresent);
        FUNCTIONS.register(TESTS[1], () -> FmGameTests::sinAspectsAreRegistered);
        FUNCTIONS.register(TESTS[2], () -> FmGameTests::shardsAreRegistered);
        FUNCTIONS.register(TESTS[3], () -> FmGameTests::forbiddenResearchResolves);
        FUNCTIONS.register(TESTS[4], () -> FmGameTests::taintedBlocksAreRegistered);
        FUNCTIONS.register(TESTS[5], () -> FmGameTests::taintedBlocksUseTaintContract);
        FUNCTIONS.register(TESTS[6], () -> FmGameTests::simpleItemsAreRegistered);
        FUNCTIONS.register(TESTS[7], () -> FmGameTests::roseAndCakeBlocksAreRegistered);
    }

    private FmGameTests() {
    }

    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
            ForbiddenMagic.id("default"), new TestEnvironmentDefinition.AllOf());
        for (String name : TESTS) {
            ResourceKey<Consumer<GameTestHelper>> function =
                ResourceKey.create(Registries.TEST_FUNCTION, ForbiddenMagic.id(name));
            event.registerTest(
                ForbiddenMagic.id(name),
                new FunctionGameTestInstance(function,
                    new TestData<>(environment, Identifier.parse("thaumaturge:empty_5x5x7"), 100, 0, true, Rotation.NONE))
            );
        }
    }

    private static void thaumaturgeHostIsPresent(GameTestHelper helper) {
        helper.assertTrue(ModList.get().isLoaded("thaumaturge"), "thaumaturge must be loaded");
        helper.succeed();
    }

    private static void sinAspectsAreRegistered(GameTestHelper helper) {
        HolderLookup.RegistryLookup<IAspect> aspects = aspects(helper);
        for (ResourceKey<IAspect> key : List.of(FmAspects.LUXURIA, FmAspects.INFERNUS, FmAspects.SUPERBIA,
            FmAspects.GULA, FmAspects.INVIDIA, FmAspects.DESIDIA, FmAspects.IRA)) {
            IAspect aspect = aspects.get(key).map(Holder.Reference::value).orElse(null);
            helper.assertTrue(aspect != null, "missing aspect " + key.identifier());
            helper.assertTrue(aspect.components().size() == 2, key.identifier() + " must have two components");
            helper.assertTrue(aspect.texture().getPath().endsWith(key.identifier().getPath() + ".png"),
                key.identifier() + " must use its own texture");
        }
        helper.succeed();
    }

    private static void shardsAreRegistered(GameTestHelper helper) {
        for (String vice : List.of("wrath", "envy", "taint", "pride", "lust", "sloth", "greed", "gluttony")) {
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(ForbiddenMagic.id(vice + "_shard")),
                vice + "_shard must be registered");
        }
        helper.succeed();
    }

    private static void forbiddenResearchResolves(GameTestHelper helper) {
        HolderLookup.RegistryLookup<IResearchCategory> categories =
            helper.getLevel().registryAccess().lookup(IResearchCategory.REGISTRY_KEY).orElse(null);
        helper.assertTrue(categories != null, "research category registry must exist");
        helper.assertTrue(categories.get(ResourceKey.create(IResearchCategory.REGISTRY_KEY, ForbiddenMagic.id("forbidden"))).isPresent(),
            "forbiddenmagic:forbidden category must exist");

        HolderLookup.RegistryLookup<IResearchEntry> entries =
            helper.getLevel().registryAccess().lookup(IResearchEntry.REGISTRY_KEY).orElse(null);
        helper.assertTrue(entries != null, "research entry registry must exist");
        helper.assertTrue(entries.get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, ForbiddenMagic.id("nether_shards"))).isPresent(),
            "forbiddenmagic:nether_shards entry must exist");
        helper.succeed();
    }

    private static void taintedBlocksAreRegistered(GameTestHelper helper) {
        for (String id : List.of("taint_log", "taint_planks", "taint_stone_smooth", "taint_stone_brick",
            "star_block", "taint_leaves", "taint_sapling")) {
            helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(ForbiddenMagic.id(id)), id + " block must be registered");
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(ForbiddenMagic.id(id)), id + " block item must be registered");
        }
        helper.succeed();
    }

    private static void taintedBlocksUseTaintContract(GameTestHelper helper) {
        for (String id : List.of("taint_log", "taint_planks", "taint_stone_smooth", "taint_stone_brick",
            "taint_leaves", "taint_sapling")) {
            Block block = BuiltInRegistries.BLOCK.getValue(ForbiddenMagic.id(id));
            helper.assertTrue(block instanceof ITaintBlock, id + " must implement ITaintBlock");
        }
        helper.succeed();
    }

    private static void simpleItemsAreRegistered(GameTestHelper helper) {
        for (String id : List.of("emerald_fragment", "taint_coal", "taint_fruit", "black_dye", "diabolist_fork")) {
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(ForbiddenMagic.id(id)), id + " must be registered");
        }
        helper.succeed();
    }

    private static void roseAndCakeBlocksAreRegistered(GameTestHelper helper) {
        for (String id : List.of("umbral_rose", "umbral_rose_bush", "arcane_cake")) {
            helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(ForbiddenMagic.id(id)), id + " block must be registered");
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(ForbiddenMagic.id(id)), id + " block item must be registered");
        }
        helper.succeed();
    }

    private static HolderLookup.RegistryLookup<IAspect> aspects(GameTestHelper helper) {
        return helper.getLevel().registryAccess().lookup(IAspect.REGISTRY_KEY).orElseThrow();
    }
}
