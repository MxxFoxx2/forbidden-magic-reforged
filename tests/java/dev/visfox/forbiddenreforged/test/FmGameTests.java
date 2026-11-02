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
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import dev.visfox.forbiddenreforged.integration.api.HostProfile;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.FmConfig;
import dev.visfox.forbiddenreforged.FmItems;
import dev.visfox.forbiddenreforged.FmCreativeTab;
import dev.visfox.forbiddenreforged.FmToolEvents;
import dev.visfox.forbiddenreforged.FmWands;
import dev.visfox.forbiddenreforged.FmComponents;
import dev.visfox.forbiddenreforged.MobCrystalItem;
import dev.visfox.forbiddenreforged.WrathCageBlock;
import dev.visfox.forbiddenreforged.WrathCageBlockEntity;
import dev.visfox.forbiddenreforged.core.block.WrathCageBehavior;
import dev.visfox.forbiddenreforged.core.wand.WitchwoodManaBehavior;
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
import thaumcraft.wand.Wands;
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
    public static void hostAdapterDeclaresThe1_21_1CapabilityProfile(GameTestHelper helper) {
        HostProfile profile = HostAdapterRegistry.require().profile();
        helper.assertValueEqual(profile.minecraftVersion(), "1.21.1",
            "the selected TC4 adapter must target Minecraft 1.21.1");
        helper.assertValueEqual(profile.hostVersion(), "1.1.2",
            "the selected adapter must identify the pinned Thaumcraft host version");
        helper.assertValueEqual(profile.features().size(), HostProfile.Feature.values().length,
            "the host profile must declare every capability");

        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.ASPECT_REGISTRATION).level(),
            HostProfile.SupportLevel.ADAPTED, "aspect registration uses the shared catalog adapter");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.ITEM_PROPERTIES).level(),
            HostProfile.SupportLevel.ADAPTED, "item properties are supplied through host capabilities");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.ITEM_FACTORIES).level(),
            HostProfile.SupportLevel.ADAPTED, "host-backed item factories are routed through capabilities");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.MORPH_TOOL_RUNTIME).level(),
            HostProfile.SupportLevel.ADAPTED, "morph tool behavior uses host services");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.TAINT_BLOCK_SERVICES).level(),
            HostProfile.SupportLevel.ADAPTED, "taint block services use the host adapter");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.TAINT_ORIGINS).level(),
            HostProfile.SupportLevel.UNSUPPORTED, "taint origins are explicitly declared unsupported");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.WAND_CAPS).level(),
            HostProfile.SupportLevel.ADAPTED, "wand cap registration is owned by the TC4 adapter");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.WAND_RODS).level(),
            HostProfile.SupportLevel.ADAPTED, "wand rod registration is owned by the TC4 adapter");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.WAND_PARTS).level(),
            HostProfile.SupportLevel.ADAPTED, "Profane Wand rod replacement uses a typed parts port");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.WAND_TRIGGERS).level(),
            HostProfile.SupportLevel.ADAPTED, "Hellfire wand trigger registration is owned by the TC4 adapter");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.VILLAGER_TRADES).level(),
            HostProfile.SupportLevel.ADAPTED, "Thaumcraft-specific heretic trades are supplied by the host adapter");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.RUNTIME_BOOTSTRAP).level(),
            HostProfile.SupportLevel.ADAPTED, "host bootstrap capability is implemented");
        int optionalEnchantments = HostAdapterRegistry.requireBootstrap().registeredOptionalEnchantments();
        int expectedOptionalEnchantments = FmConfig.enchantmentsEnabled()
            && ModList.get().isLoaded("thaumictinkerer") ? 8 : 0;
        helper.assertValueEqual(optionalEnchantments, expectedOptionalEnchantments,
            "the host bootstrap registers the complete optional Thaumic Tinkerer cost set or none");
        boolean hasTaintedCreativeWand = FmCreativeTab.stacks().stream()
            .anyMatch(stack -> Wands.isWand(stack) && "tainted".equals(Wands.getRod(stack).tag()));
        helper.assertTrue(hasTaintedCreativeWand,
            "the host bootstrap contributes a Tainted Wand to the shared creative tab");
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
        boolean am2 = ModList.get().isLoaded("arsmagica2");
        if (am2) {
            helper.assertTrue(WandRegistry.findRod("witchwood").onUpdate() != null,
                "Witchwood wand core must have its mana recharge update");
            helper.assertTrue(WandRegistry.findRod("witchwood_staff").onUpdate() != null,
                "Witchwood staff core must have its faster mana recharge update");
        }

        helper.assertTrue(WandRegistry.findRod("tainted") != null, "tainted rod must be registered");
        helper.assertTrue(WandRegistry.findRod("infernal") != null, "infernal rod must be registered");
        helper.assertTrue(WandRegistry.findRod("profane") != null, "profane rod must be registered");
        helper.assertValueEqual(WandRegistry.findRod("witchwood") != null, am2, "witchwood rod registration follows Ars Magica 2");
        helper.assertValueEqual(WandRegistry.findRod("witchwood_staff") != null, am2, "witchwood staff rod registration follows Ars Magica 2");
        helper.assertTrue(BuiltInRegistries.BLOCK_ENTITY_TYPE.get(ForbiddenMagic.id("wrath_cage")) == FmBlockEntities.WRATH_CAGE.get(),
            "Wrath Cage block entity type must be registered");
        helper.assertTrue(WrathCageBlockEntity.shouldSpawn(false, true, 0, 12, 12), "cage should spawn when fuel is available");
        helper.assertTrue(!WrathCageBlockEntity.shouldSpawn(true, true, 0, 12, 12), "redstone must disable spawning");
        helper.assertTrue(!WrathCageBlockEntity.shouldSpawn(false, false, 0, 12, 12), "unattuned cage must not spawn");
        helper.assertValueEqual(WrathCageBehavior.acceptedAmount(60, 10, 64), 4,
            "essentia insertion must respect the remaining per-channel capacity");
        helper.assertValueEqual(WrathCageBehavior.acceptedAmount(64, 10, 64), 0,
            "a full essentia channel must reject additional storage");
        helper.assertValueEqual(WrathCageBehavior.acceptedAmount(0, -5, 64), 0,
            "negative essentia requests must not change storage");
        helper.assertTrue(WrathCageBehavior.canDrawMobEssentia(63, 64),
            "mob essentia can be drawn while its channel has room");
        helper.assertTrue(!WrathCageBehavior.canDrawMobEssentia(64, 64),
            "mob essentia must stop at channel capacity");
        helper.assertTrue(WrathCageBehavior.canDrawWrathEssentia(11, 63, 64, 12),
            "Wrath can be drawn until its own channel fills or mob fuel reaches cost");
        helper.assertTrue(!WrathCageBehavior.canDrawWrathEssentia(12, 0, 64, 12),
            "Wrath extraction pauses once mob essentia alone reaches the fuel cost");
        helper.assertTrue(WrathCageBehavior.canDrawSlothEssentia(11, 11, 63, 64, 12),
            "Sloth can be drawn while both higher-priority channels are below cost");
        helper.assertTrue(!WrathCageBehavior.canDrawSlothEssentia(11, 12, 0, 64, 12),
            "Sloth extraction pauses once the Wrath channel reaches cost");
        helper.assertValueEqual(WrathCageBehavior.selectFuelChannel(12, 12, 12, 12),
            WrathCageBehavior.FuelChannel.SPECIAL, "mob aspect has first fuel priority");
        helper.assertValueEqual(WrathCageBehavior.selectFuelChannel(0, 12, 12, 12),
            WrathCageBehavior.FuelChannel.WRATH, "Wrath has second fuel priority");
        helper.assertValueEqual(WrathCageBehavior.selectFuelChannel(0, 0, 12, 12),
            WrathCageBehavior.FuelChannel.SLOTH, "Sloth is the final fuel fallback");
        helper.assertValueEqual(WrathCageBehavior.selectFuelChannel(0, 0, 11, 12),
            WrathCageBehavior.FuelChannel.NONE, "partial channels cannot fuel a spawn cycle");
        helper.assertValueEqual(WrathCageBehavior.selectFuelChannel(64, 64, 64, 0),
            WrathCageBehavior.FuelChannel.NONE, "disabled essentia cost consumes no fuel channel");
        helper.assertTrue(WrathCageBehavior.nextDelay(0, false) >= 200
            && WrathCageBehavior.nextDelay(99, false) < 300,
            "normal spawn delay must remain in the original 200..299 tick range");
        helper.assertTrue(WrathCageBehavior.nextDelay(99, true) >= 400
            && WrathCageBehavior.nextDelay(0, true) < 500,
            "slothful spawn delay must remain in the original 400..499 tick range");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void witchwoodManaCostsPreserveOriginalRates(GameTestHelper helper) {
        helper.assertTrue(WitchwoodManaBehavior.supportsArsMagicaModId("arsmagicalegacy"),
            "modern Ars Magica: Legacy mod id is recognized by shared core");
        helper.assertTrue(WitchwoodManaBehavior.supportsArsMagicaModId("arsmagica2"),
            "legacy Ars Magica 2 mod id remains supported by shared core");
        helper.assertTrue(!WitchwoodManaBehavior.supportsArsMagicaModId("unrelatedmod"),
            "unrelated mods do not activate Witchwood mana integration");
        helper.assertValueEqual(WitchwoodManaBehavior.rechargeInterval(false), 100,
            "shared Witchwood wand interval remains 100 ticks");
        helper.assertValueEqual(WitchwoodManaBehavior.rechargeInterval(true), 40,
            "shared Witchwood staff interval remains 40 ticks");
        helper.assertTrue(Math.abs(WitchwoodManaBehavior.manaCost(false, "iron", 20) - 28.0F) < 0.01F,
            "shared core scales partial wand deficits correctly");
        helper.assertTrue(Math.abs(WitchwoodManaBehavior.manaCost(true, "vinteum", 20) - 10.0F) < 0.01F,
            "shared core scales partial Vinteum staff deficits correctly");
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
        helper.assertTrue(taintLog != null && taintLog.getAmount(Aspect.TAINT) >= 1,
            "taint log block item must carry taint aspects");
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
}
