package dev.visfox.forbiddenreforged.test;

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
        helper.assertValueEqual(Aspect.builtinCount(), 48, "exactly 48 builtin aspects");
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
        helper.assertTrue(nether != null && nether.ordinal() >= Aspect.builtinCount(), "infernus must be an add-on aspect");
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
}
