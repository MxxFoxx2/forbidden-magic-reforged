package dev.visfox.forbiddenreforged.test;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.Direction;
import thaumcraft.taint.TaintSpread;
import dev.visfox.forbiddenreforged.TaintLogTextureVariant;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import dev.visfox.forbiddenreforged.FmBlocks;
import dev.visfox.forbiddenreforged.FmSpawnerMobs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.research.ResearchItem;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.FmConfig;
import dev.visfox.forbiddenreforged.FmItems;
import dev.visfox.forbiddenreforged.FmToolEvents;
import dev.visfox.forbiddenreforged.FmWands;
import dev.visfox.forbiddenreforged.FmComponents;
import dev.visfox.forbiddenreforged.MobCrystalItem;
import dev.visfox.forbiddenreforged.WrathCageBlock;
import dev.visfox.forbiddenreforged.WrathCageBlockEntity;
import dev.visfox.forbiddenreforged.FmBlockEntities;
import dev.visfox.forbiddenreforged.FmItemEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.fml.ModList;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectRegistry;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.research.ResearchCategories;
import thaumcraft.wand.WandRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;

@GameTestHolder
@PrefixGameTestTemplate(false)
public final class FmGameTests {
    private FmGameTests() {
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void modLoadsAndHostIsPresent(GameTestHelper helper) {
        helper.assertTrue(ModList.get().isLoaded("thaumcraft"), "thaumcraft must be loaded");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void taintCoalHasFuelValue(GameTestHelper helper) {
        FurnaceFuelBurnTimeEvent event = new FurnaceFuelBurnTimeEvent(
            new ItemStack(FmItems.TAINT_COAL.get()), 0, RecipeType.SMELTING
        );
        FmItemEvents.onFuel(event);
        helper.assertTrue(event.getBurnTime() >= 1 && event.getBurnTime() <= 2400,
            "taint coal burn time must remain in the original 1..2400 range");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void itemRegistryContainsPortedBaseItems(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.ITEM.get(ForbiddenMagic.id("taint_fruit")) != net.minecraft.world.item.Items.AIR,
            "taint fruit must be registered");
        helper.assertTrue(BuiltInRegistries.ITEM.get(ForbiddenMagic.id("black_dye")) != net.minecraft.world.item.Items.AIR,
            "black dye must be registered");
        helper.assertTrue(BuiltInRegistries.ITEM.get(ForbiddenMagic.id("lust_shard")) != net.minecraft.world.item.Items.AIR,
            "lust shard must be registered");
        helper.succeed();
    }


    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void taintAndUmbralBlocksAreRegistered(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.BLOCK.get(ForbiddenMagic.id("taint_log")) != Blocks.AIR,
            "taint log must be registered");
        helper.assertTrue(BuiltInRegistries.BLOCK.get(ForbiddenMagic.id("taint_sapling")) != Blocks.AIR,
            "taint sapling must be registered");
        helper.assertTrue(BuiltInRegistries.BLOCK.get(ForbiddenMagic.id("umbral_rose")) != Blocks.AIR,
            "umbral rose must be registered");
        helper.assertTrue(BuiltInRegistries.BLOCK.get(ForbiddenMagic.id("arcane_cake")) != Blocks.AIR,
            "arcane cake must be registered");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void mobCrystalAttunesAndSwapsCageTarget(GameTestHelper helper) {
        var cage = new WrathCageBlockEntity(new net.minecraft.core.BlockPos(0, 0, 0),
            dev.visfox.forbiddenreforged.FmBlocks.WRATH_CAGE.get().defaultBlockState());
        ItemStack crystal = new ItemStack(FmItems.MOB_CRYSTAL.get());
        MobCrystalItem.setMob(crystal, "minecraft:zombie");
        helper.assertTrue(WrathCageBlock.attune(cage, crystal), "valid crystal should attune cage");
        helper.assertValueEqual(cage.getMobId(), "minecraft:zombie", "cage should remember the captured entity");
        helper.assertTrue(MobCrystalItem.mob(crystal) == null, "crystal should be consumed into an empty cage");
        ItemStack second = new ItemStack(FmItems.MOB_CRYSTAL.get());
        MobCrystalItem.setMob(second, "minecraft:skeleton");
        helper.assertTrue(WrathCageBlock.attune(cage, second), "second crystal should swap cage target");
        helper.assertValueEqual(cage.getMobId(), "minecraft:skeleton", "cage should change its target");
        helper.assertValueEqual(MobCrystalItem.mob(second), "minecraft:zombie", "old target should be returned in the crystal");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void forbiddenWandRodsAreRegistered(GameTestHelper helper) {
        helper.assertTrue(WandRegistry.findRod("witchwood").onUpdate() != null,
            "Witchwood wand core must have its mana recharge update");
        helper.assertTrue(WandRegistry.findRod("witchwood_staff").onUpdate() != null,
            "Witchwood staff core must have its faster mana recharge update");

        helper.assertTrue(WandRegistry.findRod("tainted") != null, "tainted rod must be registered");
        helper.assertTrue(WandRegistry.findRod("infernal") != null, "infernal rod must be registered");
        helper.assertTrue(WandRegistry.findRod("profane") != null, "profane rod must be registered");
        helper.assertTrue(WandRegistry.findRod("witchwood") != null, "witchwood rod must be registered");
        helper.assertTrue(WandRegistry.findRod("witchwood_staff") != null, "witchwood staff rod must be registered");
        helper.assertTrue(BuiltInRegistries.BLOCK_ENTITY_TYPE.get(ForbiddenMagic.id("wrath_cage")) == FmBlockEntities.WRATH_CAGE.get(),
            "Wrath Cage block entity type must be registered");
        helper.assertTrue(WrathCageBlockEntity.shouldSpawn(false, true, 0, 12, 12), "cage should spawn when fuel is available");
        helper.assertTrue(!WrathCageBlockEntity.shouldSpawn(true, true, 0, 12, 12), "redstone must disable spawning");
        helper.assertTrue(!WrathCageBlockEntity.shouldSpawn(false, false, 0, 12, 12), "unattuned cage must not spawn");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void witchwoodManaCostsPreserveOriginalRates(GameTestHelper helper) {
        helper.assertValueEqual(FmWands.witchwoodRechargeInterval(false), 100,
            "Witchwood wand recharges on the original 100-tick interval");
        helper.assertValueEqual(FmWands.witchwoodRechargeInterval(true), 40,
            "Witchwood staff recharges faster on the original 40-tick interval");
        helper.assertTrue(Math.abs(FmWands.witchwoodManaCost(false, "iron") - 140.0F) < 0.01F,
            "ordinary wand cap costs 140 mana per vis point");
        helper.assertTrue(Math.abs(FmWands.witchwoodManaCost(true, "iron") - 100.0F) < 0.01F,
            "ordinary staff cap costs 100 mana per vis point");
        helper.assertTrue(Math.abs(FmWands.witchwoodManaCost(false, "vinteum") - 80.0F) < 0.01F,
            "Vinteum reduces wand recharge cost");
        helper.assertTrue(Math.abs(FmWands.witchwoodManaCost(true, "vinteum") - 50.0F) < 0.01F,
            "Vinteum reduces staff recharge cost");
        helper.assertTrue(Math.abs(FmWands.witchwoodManaCost(false, "iron", 20) - 28.0F) < 0.01F,
            "partially depleted wand aspect only charges for missing vis units");
        helper.assertTrue(Math.abs(FmWands.witchwoodManaCost(true, "vinteum", 20) - 10.0F) < 0.01F,
            "partial staff recharge applies the cap modifier to missing vis units");
        helper.assertTrue(Math.abs(FmWands.witchwoodManaCost(false, "iron", 150) - 140.0F) < 0.01F,
            "one update never recharges more than one vis point per aspect");
        helper.assertTrue(FmWands.witchwoodManaCost(false, "iron", 0) == 0.0F,
            "a full aspect costs no mana to recharge");
        helper.assertTrue(FmWands.supportsArsMagicaModId("arsmagicalegacy"),
            "modern Ars Magica: Legacy mod id is recognized");
        helper.assertTrue(FmWands.supportsArsMagicaModId("arsmagica2"),
            "legacy Ars Magica 2 mod id remains supported");
        helper.assertTrue(!FmWands.supportsArsMagicaModId("unrelatedmod"),
            "unrelated mods do not activate Witchwood mana integration");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void nutritionRingOnlyCountsRingSlotStacks(GameTestHelper helper) {
        ItemStack ring = new ItemStack(FmItems.RING_NUTRITION.get());
        helper.assertTrue(FmToolEvents.hasNutritionRingInRingSlots(ring, ItemStack.EMPTY),
            "nutrition ring in first ring slot grants its food bonus");
        helper.assertTrue(FmToolEvents.hasNutritionRingInRingSlots(ItemStack.EMPTY, ring),
            "nutrition ring in second ring slot grants its food bonus");
        helper.assertTrue(!FmToolEvents.hasNutritionRingInRingSlots(ItemStack.EMPTY, ItemStack.EMPTY),
            "empty ring slots grant no food bonus");
        helper.succeed();
    }


    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void forbiddenMagicResearchIsRegistered(GameTestHelper helper) {
        helper.assertTrue(ResearchCategories.get("FORBIDDEN") != null, "Forbidden research category must exist");
        helper.assertTrue(ResearchCategories.getResearch("NETHERSHARDS") != null, "Nether shards entry must exist");
        helper.assertTrue(ResearchCategories.getResearch("TAINTTREE") != null, "taint tree research must exist");
        helper.assertTrue(ResearchCategories.getResearch("BLACKFLOWER") != null, "black flower research must exist");
        helper.assertTrue(ResearchCategories.getResearch("TAINTSTONE") != null, "tainted stone research must exist");
        helper.assertTrue(ResearchCategories.getResearch("ROD_tainted") != null, "tainted rod research must exist");
        helper.assertTrue(ResearchCategories.getResearch("ROD_infernal") != null, "infernal rod research must exist");
        helper.assertTrue(ResearchCategories.getResearch("ARCANECAKE") != null, "arcane cake research must exist");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void forbiddenMagicThaumcraftRecipesAreRegistered(GameTestHelper helper) {
        helper.assertTrue(TcRecipes.get("TAINTTREE") != null, "taint sapling crucible recipe must exist");
        helper.assertTrue(TcRecipes.get("BLACKFLOWER") != null, "umbral rose crucible recipe must exist");
        helper.assertTrue(TcRecipes.get("TaintStone") != null, "tainted stone arcane recipe must exist");
        helper.assertTrue(TcRecipes.get("ARCANECAKE") != null, "Thaumic Cake infusion recipe must exist");
        helper.assertTrue(TcRecipes.get("ROD_TAINTED") != null, "tainted rod infusion recipe must exist");
        helper.assertTrue(TcRecipes.get("ROD_INFERNAL") != null, "infernal rod infusion recipe must exist");
        helper.assertTrue(TcRecipes.get("WRATHCAGE") != null, "Wrath Cage infusion recipe must exist");
        helper.assertTrue(TcRecipes.get("MOBCRYSTAL") != null, "Mob Crystal crucible recipe must exist");
        helper.assertTrue(TcIngredient.item("thaumcraft:tainted_goo").exists(), "tainted goo central ingredient must exist");
        helper.assertTrue(TcIngredient.item("thaumcraft:salis_mundus").exists(), "Salis Mundus must exist");
        helper.assertTrue(TcIngredient.item("forbiddenmagic:tainted_rod").exists(), "tainted rod result must exist");
        helper.assertTrue(TcIngredient.item("forbiddenmagic:infernal_rod").exists(), "infernal rod result must exist");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void itemAspectTagsExist(GameTestHelper helper) {
        AspectList fruit = AspectRegistry.active().getObjectTags(FmItems.TAINT_FRUIT.get());
        AspectList shard = AspectRegistry.active().getObjectTags(FmItems.DEADLY_SHARDS.get("wrath").get());
        helper.assertTrue(fruit != null && fruit.getAmount(Aspect.TAINT) >= 2,
            "taint fruit must carry taint aspects");
        helper.assertTrue(shard != null && shard.getAmount(FmAspects.WRATH) >= 2,
            "wrath shard must carry wrath aspects");
        AspectList taintLog = AspectRegistry.active().getObjectTags(
            BuiltInRegistries.ITEM.get(ForbiddenMagic.id("taint_log")));
        helper.assertTrue(taintLog != null && taintLog.getAmount(Aspect.TAINT) >= 2,
            "taint log block item must carry taint aspects");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void aspectRegistryKeepsBuiltinOrderAndCombines(GameTestHelper helper) {
        Aspect[] all = Aspect.values();
        helper.assertValueEqual(FmAspects.BUILTIN_ASPECT_COUNT, 48, "exactly 48 builtin aspects");
        helper.assertTrue(all.length >= 48, "builtins must remain registered");
        helper.assertValueEqual(all[0], Aspect.AIR, "first builtin must stay index 0");
        helper.assertValueEqual(all[47], Aspect.TRAP, "last builtin must stay index 47");
        helper.assertTrue(Aspect.combine(Aspect.AIR, Aspect.FIRE) == Aspect.LIGHT, "combine(AIR, FIRE) must be LIGHT");
        helper.assertTrue(Aspect.getAspect("aer") == Aspect.AIR, "by-tag lookup must work");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void addonAspectsSurviveSerialization(GameTestHelper helper) {
        Aspect nether = FmAspects.NETHER;
        helper.assertTrue(nether != null && nether.ordinal() >= FmAspects.BUILTIN_ASPECT_COUNT, "infernus must be an add-on aspect");
        helper.assertValueEqual(Aspect.getAspect("infernus"), nether, "by-tag lookup must find infernus");
        helper.assertTrue(FmAspects.PRIDE != null && FmAspects.GLUTTONY != null && FmAspects.ENVY != null
            && FmAspects.SLOTH != null && FmAspects.WRATH != null, "all enabled sin aspects must be registered");
        if (!FmConfig.lustDisabled()) {
            helper.assertTrue(FmAspects.LUST != null, "luxuria must register when no_lust is disabled");
        } else {
            helper.assertTrue(Aspect.getAspect("luxuria") == null, "luxuria must not register when no_lust is enabled");
        }
        AspectList list = new AspectList().add(nether, 3);
        io.netty.buffer.ByteBuf buffer = io.netty.buffer.Unpooled.buffer();
        AspectList.STREAM_CODEC.encode(buffer, list);
        buffer.readerIndex(0);
        AspectList decoded = AspectList.STREAM_CODEC.decode(buffer);
        helper.assertValueEqual(decoded.getAmount(nether), 3, "aspect amount must survive a network round trip");
        buffer.release();
        helper.succeed();
    }
    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void mobCrystalAndSpawnTable(GameTestHelper helper) {
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

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void wrathCageBlockAndEntity(GameTestHelper helper) {
        BlockPos pos = new BlockPos(5, 4, 5);
        helper.setBlock(pos, FmBlocks.WRATH_CAGE.get().defaultBlockState());
        WrathCageBlockEntity cage = (WrathCageBlockEntity) helper.getBlockEntity(pos);
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

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void wrathCageCrystalAndSpawnRules(GameTestHelper helper) {
        helper.assertFalse(WrathCageBlockEntity.shouldSpawn(true, true, 10, 5, 0), "a powered cage waits");
        helper.assertTrue(WrathCageBlockEntity.shouldSpawn(false, true, 10, 5, 0), "an attuned fuelled cage spawns");
        helper.assertFalse(WrathCageBlockEntity.shouldSpawn(false, true, 0, 5, 4), "no fuel and too little essentia waits");
        helper.assertTrue(WrathCageBlockEntity.shouldSpawn(false, true, 0, 5, 5), "enough essentia starts a fuel cycle");
        helper.assertFalse(WrathCageBlockEntity.shouldSpawn(false, false, 10, 5, 0), "an unattuned cage waits");
        helper.assertTrue(WrathCageBlockEntity.shouldSpawn(false, true, 0, 0, 0), "cost 0 needs no fuel");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void wrathCageResearchAndRecipes(GameTestHelper helper) {
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
    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void taintMaterialBlocksAreRegistered(GameTestHelper helper) {
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.TAINT_LOG.get()), ResourceLocation.parse("forbiddenmagic:taint_log"), "taint log id");
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.TAINT_PLANKS.get()), ResourceLocation.parse("forbiddenmagic:taint_planks"), "taint planks id");
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.TAINT_STONE_SMOOTH.get()), ResourceLocation.parse("forbiddenmagic:taint_stone_smooth"), "smooth stone id");
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.TAINT_STONE_BRICK.get()), ResourceLocation.parse("forbiddenmagic:taint_stone_brick"), "brick id");
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.STAR_BLOCK.get()), ResourceLocation.parse("forbiddenmagic:star_block"), "star block id");
        helper.assertFalse(new ItemStack(FmBlocks.TAINT_LOG.get().asItem()).isEmpty(), "taint log has an item");
        helper.assertFalse(new ItemStack(FmBlocks.STAR_BLOCK.get().asItem()).isEmpty(), "star block has an item");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void taintBlocksCarryToolTags(GameTestHelper helper) {
        TagKey<Block> axe = TagKey.create(Registries.BLOCK, ResourceLocation.parse("minecraft:mineable/axe"));
        TagKey<Block> pickaxe = TagKey.create(Registries.BLOCK, ResourceLocation.parse("minecraft:mineable/pickaxe"));
        TagKey<Block> beacon = TagKey.create(Registries.BLOCK, ResourceLocation.parse("minecraft:beacon_base_blocks"));
        helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.TAINT_LOG.get()).is(axe), "taint log mines with an axe");
        helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.TAINT_PLANKS.get()).is(axe), "taint planks mine with an axe");
        helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.TAINT_STONE_SMOOTH.get()).is(pickaxe), "taint stone mines with a pickaxe");
        helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.STAR_BLOCK.get()).is(beacon), "star block is a beacon base");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void taintMaterialsResistFibrousTaint(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
        for (Block block : List.of(
            FmBlocks.TAINT_LOG.get(),
            FmBlocks.TAINT_PLANKS.get(),
            FmBlocks.TAINT_STONE_SMOOTH.get(),
            FmBlocks.TAINT_STONE_BRICK.get()
        )) {
            level.setBlockAndUpdate(pos, block.defaultBlockState());
            level.setBlockAndUpdate(pos.above(), Blocks.STONE.defaultBlockState());
            helper.assertFalse(TaintSpread.spreadFibres(level, pos),
                BuiltInRegistries.BLOCK.getKey(block) + " must not be replaced by fibrous taint");
            helper.assertTrue(level.getBlockState(pos).is(block),
                BuiltInRegistries.BLOCK.getKey(block) + " remains intact after a spread attempt");
        }
        helper.assertFalse(FmBlocks.TAINT_LOG.get().defaultBlockState().is(BlockTags.LOGS),
            "taint log must not enter the host vanilla-log crusting path");
        for (Block block : List.of(FmBlocks.TAINT_PLANKS.get(), FmBlocks.TAINT_STONE_SMOOTH.get(), FmBlocks.TAINT_STONE_BRICK.get())) {
            var state = block.defaultBlockState();
            helper.assertFalse(state.is(BlockTags.DIRT) || state.is(BlockTags.SAND),
                BuiltInRegistries.BLOCK.getKey(block) + " must not enter the host soil conversion path");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void blocksDropThemselves(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(0, 4, 0));
        for (var block : List.of(
            FmBlocks.TAINT_LOG.get(), FmBlocks.TAINT_PLANKS.get(),
            FmBlocks.TAINT_STONE_SMOOTH.get(), FmBlocks.TAINT_STONE_BRICK.get(),
            FmBlocks.STAR_BLOCK.get())) {
            level.setBlockAndUpdate(pos, block.defaultBlockState());
            var drops = Block.getDrops(level.getBlockState(pos), level, pos, null);
            helper.assertTrue(drops.stream().anyMatch(stack -> stack.is(block.asItem())), BuiltInRegistries.BLOCK.getKey(block) + " must drop itself");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void umbralPlantsAreRegistered(GameTestHelper helper) {
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.UMBRAL_ROSE.get()), ResourceLocation.parse("forbiddenmagic:umbral_rose"), "umbral rose id");
        helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.UMBRAL_ROSE_BUSH.get()), ResourceLocation.parse("forbiddenmagic:umbral_rose_bush"), "umbral rose bush id");
        TagKey<Block> flowers = TagKey.create(Registries.BLOCK, ResourceLocation.parse("minecraft:small_flowers"));
        helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.UMBRAL_ROSE.get()).is(flowers), "umbral rose is a small flower");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void taintLogVariantsAreWeightedAndDeterministic(GameTestHelper helper) {
        try (InputStream stream = ForbiddenMagic.class.getResourceAsStream("/assets/forbiddenmagic/blockstates/taint_log.json")) {
            helper.assertTrue(stream != null, "taint log blockstate must ship with the mod");
            JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (String axis : List.of("axis=x", "axis=y", "axis=z")) {
                var variants = root.getAsJsonObject("variants").getAsJsonArray(axis);
                helper.assertValueEqual(variants.size(), 3, axis + " keeps the three original side variants");
                int total = 0;
                for (var element : variants) {
                    total += element.getAsJsonObject().get("weight").getAsInt();
                }
                helper.assertValueEqual(total, 9, axis + " weights keep the original 75/12.5/12.5 ratio");
            }
            int[] counts = new int[3];
            for (int x = -16; x <= 16; x++) {
                for (int z = -16; z <= 16; z++) {
                    for (int side = 0; side < 6; side++) {
                        int selected = TaintLogTextureVariant.select(side, x, 7, z);
                        helper.assertTrue(selected >= 0 && selected <= 2, "face texture selector must return a known variant");
                        helper.assertValueEqual(selected, TaintLogTextureVariant.select(side, x, 7, z),
                            "face texture selection must be stable for a given side and position");
                        counts[selected]++;
                    }
                }
            }
            for (int variant = 0; variant < counts.length; variant++) {
                helper.assertTrue(counts[variant] > 0, "coordinate sample must exercise side-texture variant " + variant);
            }
            helper.assertValueEqual(TaintLogTextureVariant.select(3, 3, 7, 5), TaintLogTextureVariant.GROSS,
                "selection must use the original side + y + x*z seed");
            helper.assertValueEqual(TaintLogTextureVariant.variantForDirection(Direction.Axis.Y, Direction.DOWN, 3, 7, 5),
                TaintLogTextureVariant.STANDARD, "vertical log ends keep the end texture");
            helper.succeed();
        } catch (IOException e) {
            helper.fail("could not read the taint log blockstate: " + e);
        }
    }
    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void researchIconsResolveToRealContent(GameTestHelper helper) {
        List<String> broken = new java.util.ArrayList<>();
        for (ResearchItem item : ResearchCategories.allResearch()) {
            if (!"FORBIDDEN".equals(item.category())) {
                continue;
            }
            if (item.iconTexture() != null) {
                continue;
            }
            var icon = item.iconItem();
            if (icon == null || !icon.exists()) {
                broken.add(item.key() + " -> " + (icon == null ? "no icon" : icon.item().toString()));
            }
        }
        helper.assertTrue(broken.isEmpty(), "research icons must resolve to a real item or texture: " + broken);
        helper.succeed();
    }
}
