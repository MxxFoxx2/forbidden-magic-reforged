package dev.visfox.forbiddenreforged.test;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.FmBlocks;
import dev.visfox.forbiddenreforged.FmItems;
import dev.visfox.forbiddenreforged.FmSpawnerMobs;
import dev.visfox.forbiddenreforged.MobCrystalItem;
import dev.visfox.forbiddenreforged.WrathCageBlock;
import dev.visfox.forbiddenreforged.WrathCageBlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class EntityGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
        DeferredRegister.create(Registries.TEST_FUNCTION, ForbiddenMagic.MODID);

    private static final String[] TESTS = {
        "mob_crystal_and_spawn_table",
        "wrath_cage_block_and_entity",
        "wrath_cage_crystal_and_spawn_rules",
        "wrath_cage_research_and_recipes"
    };

    static {
        FUNCTIONS.register(TESTS[0], () -> EntityGameTests::mobCrystalAndSpawnTable);
        FUNCTIONS.register(TESTS[1], () -> EntityGameTests::wrathCageBlockAndEntity);
        FUNCTIONS.register(TESTS[2], () -> EntityGameTests::wrathCageCrystalAndSpawnRules);
        FUNCTIONS.register(TESTS[3], () -> EntityGameTests::wrathCageResearchAndRecipes);
    }

    private EntityGameTests() {}

    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
            Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, "fm_entities"), new TestEnvironmentDefinition.AllOf());
        for (String name : TESTS) {
            ResourceKey<Consumer<GameTestHelper>> function =
                ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, name));
            event.registerTest(
                Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, name),
                new FunctionGameTestInstance(function, new TestData<>(environment, Identifier.parse("thaumcraft:test/platform"), 100, 0, true))
            );
        }
    }

    private static void mobCrystalAndSpawnTable(GameTestHelper helper) {
        helper.assertTrue(FmSpawnerMobs.aspect("minecraft:zombie") == Aspect.FLESH, "zombie carries flesh");
        helper.assertTrue(FmSpawnerMobs.aspect("thaumcraft:brainy_zombie") == Aspect.MIND, "brainy zombie carries mind");
        helper.assertTrue(FmSpawnerMobs.aspect("minecraft:pig") == Aspect.BEAST, "pig carries beast");
        helper.assertValueEqual(FmSpawnerMobs.ids().size(), 42, "the table keeps exactly the original 42 mobs");
        for (String id : FmSpawnerMobs.ids()) {
            helper.assertTrue(FmSpawnerMobs.aspect(id) != null, id + " must map to an aspect");
        }
        helper.assertTrue(FmSpawnerMobs.isBoss(EntityType.ENDER_DRAGON), "the ender dragon is a boss");
        helper.assertFalse(FmSpawnerMobs.isBoss(EntityType.ZOMBIE), "a zombie is not a boss");
        ItemStack stack = new ItemStack(FmItems.MOB_CRYSTAL.get());
        helper.assertTrue(MobCrystalItem.mob(stack) == null, "a fresh crystal is empty");
        MobCrystalItem.setMob(stack, "minecraft:zombie");
        helper.assertValueEqual(MobCrystalItem.mob(stack), "minecraft:zombie", "the crystal keeps the mob id");
        helper.assertTrue(FmSpawnerMobs.canImprint(EntityType.ZOMBIE), "zombies can be imprinted");
        helper.assertFalse(FmSpawnerMobs.canImprint(EntityType.ENDER_DRAGON), "bosses cannot be imprinted");
        helper.succeed();
    }

    private static void wrathCageBlockAndEntity(GameTestHelper helper) {
        BlockPos pos = new BlockPos(5, 4, 5);
        helper.setBlock(pos, FmBlocks.WRATH_CAGE.get().defaultBlockState());
        WrathCageBlockEntity cage = helper.getBlockEntity(pos, WrathCageBlockEntity.class);
        helper.assertTrue(cage != null, "cage creates its block entity");
        helper.assertTrue(cage != null && cage.getMobId() == null, "a fresh cage is unattuned");
        ItemStack zombieCrystal = new ItemStack(FmItems.MOB_CRYSTAL.get());
        MobCrystalItem.setMob(zombieCrystal, "minecraft:zombie");
        helper.assertTrue(WrathCageBlock.attune(cage, zombieCrystal), "a zombie crystal attunes the cage");
        helper.assertValueEqual(cage.getMobId(), "minecraft:zombie", "the cage keeps the mob id");
        helper.assertTrue(MobCrystalItem.mob(zombieCrystal) == null, "attuning an empty cage blanks the crystal");
        ItemStack skeletonCrystal = new ItemStack(FmItems.MOB_CRYSTAL.get());
        MobCrystalItem.setMob(skeletonCrystal, "minecraft:skeleton");
        helper.assertTrue(WrathCageBlock.attune(cage, skeletonCrystal), "a skeleton crystal re-attunes the cage");
        helper.assertValueEqual(cage.getMobId(), "minecraft:skeleton", "the cage swaps to the new mob");
        helper.assertValueEqual(MobCrystalItem.mob(skeletonCrystal), "minecraft:zombie", "the held crystal gets the previous mob");
        ItemStack crystal = WrathCageBlockEntity.crystalForBreak(cage);
        helper.assertValueEqual(MobCrystalItem.mob(crystal), "minecraft:skeleton", "breaking the cage yields the attuned crystal");
        helper.destroyBlock(pos);
        ItemEntity dropped = helper.getEntities(EntityType.ITEM, pos, 1.0).stream().findFirst().orElse(null);
        helper.assertTrue(dropped != null && "minecraft:skeleton".equals(MobCrystalItem.mob(dropped.getItem())), "removing the cage drops its crystal");
        helper.succeed();
    }

    private static void wrathCageCrystalAndSpawnRules(GameTestHelper helper) {
        helper.assertFalse(WrathCageBlockEntity.shouldSpawn(true, true, 10, 5, 0), "a powered cage waits");
        helper.assertTrue(WrathCageBlockEntity.shouldSpawn(false, true, 10, 5, 0), "an attuned fuelled cage spawns");
        helper.assertFalse(WrathCageBlockEntity.shouldSpawn(false, true, 0, 5, 4), "no fuel and too little essentia waits");
        helper.assertTrue(WrathCageBlockEntity.shouldSpawn(false, true, 0, 5, 5), "enough essentia starts a fuel cycle");
        helper.assertFalse(WrathCageBlockEntity.shouldSpawn(false, false, 10, 5, 0), "an unattuned cage waits");
        helper.assertTrue(WrathCageBlockEntity.shouldSpawn(false, true, 0, 0, 0), "cost 0 needs no fuel");
        helper.succeed();
    }

    private static void wrathCageResearchAndRecipes(GameTestHelper helper) {
        ResearchItem cage = ResearchCategories.getResearch("WRATHCAGE");
        helper.assertTrue(cage != null, "wrath cage research exists");
        helper.assertValueEqual(cage.warp(), 3, "wrath cage warp is 3");
        helper.assertValueEqual(cage.pages().size(), 5, "wrath cage has 5 pages");
        InfusionRecipe cageRecipe = (InfusionRecipe) TcRecipes.get("WRATHCAGE");
        helper.assertTrue(cageRecipe != null, "wrath cage infusion recipe exists");
        helper.assertTrue(cageRecipe.aspects().getAmount(FmAspects.WRATH) >= 32, "wrath cage recipe needs WRATH 32");
        CrucibleRecipe crystalRecipe = (CrucibleRecipe) TcRecipes.get("MOBCRYSTAL");
        helper.assertTrue(crystalRecipe != null, "mob crystal crucible recipe exists");
        helper.assertTrue(crystalRecipe.aspects().getAmount(Aspect.MIND) >= 10, "mob crystal recipe needs MIND 10");
        helper.succeed();
    }
}
