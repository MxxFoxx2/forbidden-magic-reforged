package dev.visfox.forbiddenreforged.test;

import com.google.gson.JsonArray;
import net.minecraft.world.item.Rarity;
import thaumcraft.item.bauble.VisAmuletItem;
import thaumcraft.registry.ModEntities;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.ModList;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4AspectAdapter;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.FmComponents;
import dev.visfox.forbiddenreforged.FmDrops;
import dev.visfox.forbiddenreforged.FmItems;
import dev.visfox.forbiddenreforged.FmConfig;
import dev.visfox.forbiddenreforged.MorphState;
import dev.visfox.forbiddenreforged.FmEnchantmentEffects;
import dev.visfox.forbiddenreforged.FmToolEvents;
import dev.visfox.forbiddenreforged.RidingCropItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.research.ResearchCategories;
import thaumcraft.platform.Baubles;
import thaumcraft.wand.VisStorage;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.WandRod;
import thaumcraft.wand.Wands;

final class ItemMechanicsGameTests {
    private ItemMechanicsGameTests() {
    }

    static void skullAxeInfusionRecipeMatchesOriginal(GameTestHelper helper) {
        InfusionRecipe recipe = (InfusionRecipe) TcRecipes.get("SKULLAXE");
        helper.assertTrue(recipe != null, "skull axe recipe must be registered");
        helper.assertValueEqual(recipe.instability(), 1, "instability");
        helper.assertValueEqual(recipe.aspects().getAmount(Tc4AspectAdapter.resolveAspect(FmAspects.WRATH)), 8, "wrath cost");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.WEAPON), 8, "weapon cost");
        helper.assertValueEqual(recipe.aspects().getAmount(Tc4AspectAdapter.resolveAspect(FmAspects.NETHER)), 8, "nether cost");
        helper.assertValueEqual(recipe.components().size(), 4, "four components");
        helper.succeed();
    }

    static void morphPhaseTintsMatchOriginal(GameTestHelper helper) {
        MorphState state = MorphState.EMPTY;
        int[] colors = {0xFF980000, 0xFF0010CC, 0xFFE5DA00};
        for (int i = 0; i < colors.length; i++) {
            helper.assertValueEqual(state.tintColor(), colors[i], "morph phase " + i + " tint");
            state = state.advance();
        }
        for (String name : List.of("morph_pickaxe", "morph_sword", "morph_shovel", "morph_axe")) {
            JsonObject definition = GameTestResources.readJson(helper, "/assets/forbiddenmagic/items/" + name + ".json");
            JsonObject model = definition.getAsJsonObject("model");
            JsonArray tints = model.getAsJsonArray("tints");
            helper.assertValueEqual(tints.size(), 2, name + " has a base tint and a phase tint");
            helper.assertValueEqual(tints.get(0).getAsJsonObject().get("type").getAsString(), "minecraft:constant", name + " keeps the base layer white");
            helper.assertValueEqual(tints.get(0).getAsJsonObject().get("value").getAsInt(), 16777215, name + " base layer remains untinted");
            helper.assertValueEqual(tints.get(1).getAsJsonObject().get("type").getAsString(), "forbiddenmagic:morph_phase", name + " overlay tint uses the phase source");
        }
        helper.succeed();
    }

    static void toolAndSkullAxeChanceRulesPreserveOriginalBoundaries(GameTestHelper helper) {
        helper.assertValueEqual(FmToolEvents.taintPickaxeSpeed(0.0F), 0.0F, "zero-hardness blocks remain instant");
        helper.assertValueEqual(FmToolEvents.taintPickaxeSpeed(2.0F), 0.1F, "hardness below five uses the original slow speed");
        helper.assertValueEqual(FmToolEvents.taintPickaxeSpeed(10.0F), 5.0F, "hardness below twenty is halved");
        helper.assertValueEqual(FmToolEvents.taintPickaxeSpeed(20.0F), 25.0F, "hardness twenty and above adds five");

        helper.assertTrue(FmToolEvents.taintShardRoll(1, 0, false), "taint shard roll succeeds at base threshold");
        helper.assertFalse(FmToolEvents.taintShardRoll(2, 0, false), "taint shard roll fails above base threshold");
        helper.assertTrue(FmToolEvents.taintShardRoll(7, 0, true), "silk touch adds six to taint shard threshold");
        helper.assertTrue(FmToolEvents.taintShardRoll(3, 1, false), "Fortune adds two per level to taint shard threshold");
        helper.assertFalse(FmToolEvents.taintShardRoll(4, 1, false), "taint shard threshold excludes the next roll");

        helper.assertTrue(FmEnchantmentEffects.skullAxeRoll(3, 0, 3, 1), "skeleton threshold includes roll three");
        helper.assertFalse(FmEnchantmentEffects.skullAxeRoll(4, 0, 3, 1), "skeleton threshold excludes roll four");
        helper.assertTrue(FmEnchantmentEffects.skullAxeRoll(4, 1, 3, 1), "Looting adds one to skeleton threshold");
        helper.assertTrue(FmEnchantmentEffects.skullAxeRoll(2, 0, 2, 2), "zombie threshold includes roll two");
        helper.assertFalse(FmEnchantmentEffects.skullAxeRoll(3, 0, 2, 2), "zombie threshold excludes roll three");
        helper.assertTrue(FmEnchantmentEffects.skullAxeRoll(4, 1, 2, 2), "Looting II raises zombie threshold by four");
        helper.assertTrue(FmEnchantmentEffects.skullAxeRoll(1, 0, 1, 1), "player head threshold includes roll one");
        helper.assertFalse(FmEnchantmentEffects.skullAxeRoll(3, 1, 1, 1), "player head threshold excludes roll three at Looting I");

        WandRod infernalRod = WandRegistry.findRod("infernal");
        helper.assertTrue(infernalRod != null, "infernal rod must be registered");
        ItemStack infernalWand = Wands.create(WandRegistry.CAP_IRON, infernalRod, false);
        helper.assertTrue(FmToolEvents.cancelsInfernalDamage(infernalWand, true),
            "infernal wand cancels incoming fire or wither damage");
        helper.assertFalse(FmToolEvents.cancelsInfernalDamage(infernalWand, false),
            "infernal wand does not cancel unrelated incoming damage");
        WandRod taintedRod = WandRegistry.findRod("tainted");
        ItemStack taintedWand = Wands.create(WandRegistry.CAP_IRON, taintedRod, false);
        helper.assertFalse(FmToolEvents.cancelsInfernalDamage(taintedWand, true),
            "tainted wand does not inherit infernal immunity");
        helper.assertFalse(FmToolEvents.cancelsInfernalDamage(new ItemStack(Items.STICK), true),
            "ordinary items do not cancel fire damage");
        helper.succeed();
    }

    static void silverfishEmeraldDropThresholdsMatchOriginal(GameTestHelper helper) {
        helper.assertTrue(FmEnchantmentEffects.silverfishEmeraldRoll(true, 2, 0), "Windswept Hills includes roll 2 at Looting 0");
        helper.assertFalse(FmEnchantmentEffects.silverfishEmeraldRoll(true, 3, 0), "Windswept Hills excludes roll 3 at Looting 0");
        helper.assertTrue(FmEnchantmentEffects.silverfishEmeraldRoll(true, 4, 1), "Windswept Hills adds two per Looting level");
        helper.assertFalse(FmEnchantmentEffects.silverfishEmeraldRoll(true, 5, 1), "Windswept Hills excludes the next roll");

        helper.assertTrue(FmEnchantmentEffects.silverfishEmeraldRoll(false, 1, 0), "other biomes include roll 1 at Looting 0");
        helper.assertFalse(FmEnchantmentEffects.silverfishEmeraldRoll(false, 2, 0), "other biomes exclude roll 2 at Looting 0");
        helper.assertTrue(FmEnchantmentEffects.silverfishEmeraldRoll(false, 4, 1), "other biomes add three per Looting level");
        helper.assertFalse(FmEnchantmentEffects.silverfishEmeraldRoll(false, 5, 1), "other biomes exclude the next roll");
        helper.succeed();
    }

    static void subcollarPlacesOnAnotherPlayerAndStoresDamageVis(GameTestHelper helper) {
        if (FmConfig.lustDisabled()) {
            helper.succeed();
            return;
        }
        Item item = BuiltInRegistries.ITEM.getValue(ForbiddenMagic.id("sub_collar"));
        helper.assertTrue(item != Items.AIR, "the collar item exists");
        ServerPlayer owner = GameTestPlayers.connected(helper);
        ServerPlayer target = GameTestPlayers.connected(helper);
        ItemStack collar = new ItemStack(item);
        helper.assertTrue(item instanceof VisAmuletItem, "Sub Collar is a TC4 vis amulet item");
        helper.assertTrue(((VisAmuletItem) item).canEquip(collar, target), "Sub Collar permits equipping on another player");
        helper.assertTrue(item.interactLivingEntity(collar, owner, target, InteractionHand.MAIN_HAND).consumesAction(), "the collar can be placed on another player");
        ItemStack worn = Baubles.get(target, 0);
        helper.assertTrue(worn.is(item), "the target wears the collar in the amulet slot");
        helper.assertValueEqual(worn.get(FmComponents.COLLAR_OWNER.get()), owner.getDisplayName().getString(), "the collar records its owner");
        helper.assertTrue(collar.isEmpty(), "placing consumes the collar");

        FmToolEvents.collarDamage(target, 2.0F, null);
        helper.assertValueEqual(VisStorage.getAllVis(worn).visSize(), 6, "two damage stores six vis");

        ServerPlayer attacker = GameTestPlayers.connected(helper);
        attacker.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(FmItems.RIDING_CROP.get()));
        FmToolEvents.collarDamage(target, 2.0F, attacker);
        helper.assertValueEqual(VisStorage.getAllVis(worn).visSize(), 15, "a crop attacker adds three more doses");
        helper.succeed();
    }

    static void ridingCropEffectsMatchOriginal(GameTestHelper helper) {
        ItemStack crop = new ItemStack(FmItems.RIDING_CROP.get());
        ServerPlayer attacker = GameTestPlayers.connected(helper);

        Horse horse = helper.spawn(EntityTypes.HORSE, new BlockPos(2, 4, 2));
        crop.getItem().hurtEnemy(crop, horse, attacker);
        MobEffectInstance horseSpeed = horse.getEffect(MobEffects.SPEED);
        helper.assertTrue(horseSpeed != null && horseSpeed.getDuration() == 200 && horseSpeed.getAmplifier() == 5, "a horse gets speed 200 at amplifier 5");

        Pig pig = helper.spawn(EntityTypes.PIG, new BlockPos(4, 4, 4));
        crop.getItem().hurtEnemy(crop, pig, attacker);
        MobEffectInstance pigSpeed = pig.getEffect(MobEffects.SPEED);
        helper.assertTrue(pigSpeed != null && pigSpeed.getDuration() == 200 && pigSpeed.getAmplifier() == 5, "a pig gets speed 200 at amplifier 5");

        ServerPlayer victim = GameTestPlayers.connected(helper);
        crop.getItem().hurtEnemy(crop, victim, attacker);
        helper.assertTrue(victim.hasEffect(MobEffects.SPEED) && victim.hasEffect(MobEffects.HASTE) && victim.hasEffect(MobEffects.STRENGTH), "players get speed, haste and strength");

        var golem = helper.spawn(ModEntities.GOLEM.get(), new BlockPos(7, 4, 7));
        crop.getItem().hurtEnemy(crop, golem, attacker);
        helper.assertTrue(golem.hasEffect(MobEffects.SPEED) && golem.hasEffect(MobEffects.HASTE)
            && golem.hasEffect(MobEffects.STRENGTH), "TC4 golems get speed, haste and strength");

        helper.assertTrue(RidingCropItem.dropsLustShard(1), "roll 1 drops a shard");
        helper.assertFalse(RidingCropItem.dropsLustShard(0) || RidingCropItem.dropsLustShard(2) || RidingCropItem.dropsLustShard(14), "only roll 1 of 15 drops a shard");

        helper.assertValueEqual(crop.getMaxDamage(), 59, "the crop uses the wooden sword durability");
        net.minecraft.tags.TagKey<Item> repair = net.minecraft.tags.TagKey.create(Registries.ITEM, ForbiddenMagic.id("riding_crop_repair"));
        helper.assertTrue(new ItemStack(Items.LEATHER).is(repair), "leather repairs the crop");
        helper.assertTrue(new ItemStack(Items.OAK_PLANKS).is(repair), "wooden planks repair the crop");
        helper.assertValueEqual(crop.getRarity(), Rarity.COMMON, "the crop keeps the original common rarity");
        helper.assertValueEqual(crop.getItem().getDestroySpeed(crop, Blocks.STONE.defaultBlockState()), 1.0F, "the crop always digs at speed 1");

        Horse mount = helper.spawn(EntityTypes.HORSE, new BlockPos(6, 4, 6));
        float health = mount.getHealth();
        attacker.startRiding(mount);
        crop.getItem().use(helper.getLevel(), attacker, InteractionHand.MAIN_HAND);
        MobEffectInstance mountSpeed = mount.getEffect(MobEffects.SPEED);
        helper.assertTrue(mountSpeed != null && mountSpeed.getDuration() == 200 && mountSpeed.getAmplifier() == 5, "right-click while riding gives the mount speed 200 at amplifier 5");
        helper.assertValueEqual(mount.getHealth(), health - 1.0F, "right-click while riding deals one damage to the mount");
        helper.succeed();
    }

    static void hereticTradesMatchOriginalListing(GameTestHelper helper) {
        JsonObject level1 = GameTestResources.readJson(helper, "/data/forbiddenmagic/trade_set/heretic/level_1.json");
        helper.assertValueEqual(level1.get("amount").getAsInt(), 9, "all nine heretic offers are available from the first level");
        for (int level = 2; level <= 5; level++) {
            JsonObject set = GameTestResources.readJson(helper, "/data/forbiddenmagic/trade_set/heretic/level_" + level + ".json");
            helper.assertValueEqual(set.get("amount").getAsInt(), 0, "level " + level + " adds no extra offers");
        }

        JsonArray pool = GameTestResources.readJson(helper, "/data/forbiddenmagic/tags/villager_trade/heretic/pool.json").getAsJsonArray("values");
        helper.assertValueEqual(pool.size(), 9, "the lust pool lists nine offers");
        Set<String> unique = new HashSet<>();
        for (var value : pool) {
            unique.add(value.getAsString());
        }
        helper.assertValueEqual(unique.size(), 9, "the lust pool has no duplicate offers");
        JsonArray noLustPool = GameTestResources.readJson(helper, "/data/forbiddenmagic/tags/villager_trade/heretic/no_lust_pool.json").getAsJsonArray("values");
        helper.assertValueEqual(noLustPool.size(), 8, "the no_lust pool lists eight offers");
        JsonObject noLustLevel1 = GameTestResources.readJson(helper, "/data/forbiddenmagic/trade_set/heretic/no_lust/level_1.json");
        helper.assertValueEqual(noLustLevel1.get("amount").getAsInt(), 9, "the no_lust profession also offers everything at once");
        for (int level = 2; level <= 5; level++) {
            JsonObject set = GameTestResources.readJson(helper, "/data/forbiddenmagic/trade_set/heretic/no_lust/level_" + level + ".json");
            helper.assertValueEqual(set.get("amount").getAsInt(), 0, "no_lust level " + level + " adds no extra offers");
        }

        String[] normal = {
            "emerald_greed_shard", "lust_shard_emerald", "wrath_shard_emerald", "gluttony_shard_emerald",
            "emerald_skull", "emerald_ghast_tear", "emerald_glowstone"
        };
        for (String name : normal) {
            JsonObject trade = GameTestResources.readJson(helper, "/data/forbiddenmagic/villager_trade/heretic/" + name + ".json");
            helper.assertValueEqual(trade.get("max_uses").getAsInt(), 7, name + " keeps the original seven uses");
            helper.assertValueEqual(trade.get("xp").getAsInt(), 0, name + " grants no trade xp");
            helper.assertValueEqual(trade.get("reputation_discount").getAsDouble(), 0.0, name + " has no reputation discount");
        }
        for (String name : new String[]{"emerald_profane_wand", "emerald_fire_focus"}) {
            JsonObject trade = GameTestResources.readJson(helper, "/data/forbiddenmagic/villager_trade/heretic/" + name + ".json");
            helper.assertValueEqual(trade.get("max_uses").getAsInt(), 1, name + " keeps the original single use");
            helper.assertValueEqual(trade.get("xp").getAsInt(), 0, name + " grants no trade xp");
        }
        helper.succeed();
    }

    static void wrathShardFormulaMatchesOriginal(GameTestHelper helper) {
        helper.assertValueEqual(FmDrops.heldWrathDamage(new ItemStack(Items.WOODEN_SWORD)), 4, "wooden sword adds four wrath");
        helper.assertValueEqual(FmDrops.heldWrathDamage(new ItemStack(Items.IRON_SWORD)), 6, "iron sword adds six wrath");
        helper.assertValueEqual(FmDrops.heldWrathDamage(new ItemStack(Items.DIAMOND_SWORD)), 7, "diamond sword adds seven wrath");
        helper.assertValueEqual(FmDrops.heldWrathDamage(new ItemStack(Items.IRON_PICKAXE)), 0, "tools add no wrath damage");
        helper.assertValueEqual(FmDrops.heldWrathDamage(new ItemStack(FmItems.DIABOLIST_FORK.get())), 6, "the fork counts as its sword damage");
        helper.assertValueEqual(FmDrops.heldWrathDamage(new ItemStack(FmItems.SKULL_AXE.get())), 7, "the skull axe counts as elemental sword damage");
        helper.assertValueEqual(FmDrops.heldWrathDamage(new ItemStack(FmItems.MORPH_SWORD.get())), 7, "the morph sword counts as elemental sword damage");
        helper.assertValueEqual(FmDrops.heldWrathDamage(new ItemStack(FmItems.RIDING_CROP.get())), 4, "the riding crop counts as wooden sword damage");
        helper.assertTrue(FmDrops.wrathEligible(helper.spawn(EntityTypes.ZOMBIE, new BlockPos(2, 4, 2))), "hostile mobs are eligible");
        helper.assertFalse(FmDrops.wrathEligible(helper.spawn(EntityTypes.PIG, new BlockPos(4, 4, 4))), "passive animals are not eligible");
        helper.succeed();
    }

    static void witchwoodRequiresArsMagica(GameTestHelper helper) {
        boolean am2 = ModList.get().isLoaded("arsmagica2");
        helper.assertValueEqual(BuiltInRegistries.ITEM.getOptional(ForbiddenMagic.id("witchwood_rod")).isPresent(), am2, "the witchwood rod exists only with Ars Magica 2");
        helper.assertValueEqual(BuiltInRegistries.ITEM.getOptional(ForbiddenMagic.id("witchwood_staff_rod")).isPresent(), am2, "the witchwood staff rod exists only with Ars Magica 2");
        helper.assertValueEqual(ResearchCategories.getResearch("ROD_witchwood") != null, am2, "the witchwood research exists only with Ars Magica 2");
        helper.assertValueEqual(ResearchCategories.getResearch("ROD_witchwood_staff") != null, am2, "the witchwood staff research exists only with Ars Magica 2");
        helper.succeed();
    }
}
