package dev.visfox.forbiddenreforged.test;

import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.FmItems;
import dev.visfox.forbiddenreforged.FmCreativeTab;
import dev.visfox.forbiddenreforged.FmToolEvents;
import dev.visfox.forbiddenreforged.RidingCropItem;
import thaumcraft.platform.Baubles;
import thaumcraft.platform.Bauble;
import thaumcraft.platform.BaubleType;
import thaumcraft.item.gear.RunicArmor;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.WandCap;

final class ItemRegistrationGameTests {
    private ItemRegistrationGameTests() {
    }

    static void taintCoalIsRegisteredAndIsFuel(GameTestHelper helper) {
            ItemStack stack = new ItemStack(FmItems.TAINT_COAL.get());
            helper.assertFalse(stack.isEmpty(), "taint coal must exist");
            helper.assertValueEqual(BuiltInRegistries.ITEM.getKey(stack.getItem()), Identifier.parse("forbiddenmagic:taint_coal"), "taint coal id");
            helper.assertTrue(stack.has(DataComponents.COOKING_FUEL), "taint coal is a fuel");
            helper.succeed();
        }
    
    static void taintCoalBurnTimeVariesWithinOriginalRange(GameTestHelper helper) {
            ItemStack stack = new ItemStack(FmItems.TAINT_COAL.get());
            CookingFuel fuel = stack.get(DataComponents.COOKING_FUEL);
            helper.assertTrue(fuel != null, "taint coal carries a cooking fuel component");
            LootContext context = new LootContext.Builder(
                new LootParams.Builder(helper.getLevel()).create(LootContextParamSets.EMPTY)
            ).create(Optional.empty());
            Set<Integer> drawn = new HashSet<>();
            for (int i = 0; i < 40; i++) {
                int burn = fuel.burnTime().get(context, 0);
                helper.assertTrue(burn >= 1 && burn <= 2400, "burn time " + burn + " must stay within 1..2400");
                drawn.add(burn);
            }
            helper.assertTrue(drawn.size() > 1, "burn time must be drawn per use, not a constant");
            helper.assertTrue(ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack).result().isPresent(), "taint coal must serialize its fuel component");
            helper.succeed();
        }
    
    static void emeraldFragmentIsRegistered(GameTestHelper helper) {
            ItemStack stack = new ItemStack(FmItems.EMERALD_FRAGMENT.get());
            helper.assertFalse(stack.isEmpty(), "emerald fragment must exist");
            helper.assertValueEqual(BuiltInRegistries.ITEM.getKey(stack.getItem()), Identifier.parse("forbiddenmagic:emerald_fragment"), "emerald fragment id");
            helper.succeed();
        }
    
    static void ridingCropIsRegisteredAsSword(GameTestHelper helper) {
            Item crop = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, "riding_crop"));
            helper.assertTrue(crop != Items.AIR, "riding crop must be registered");
            helper.assertTrue(crop instanceof RidingCropItem, "riding crop must be RidingCropItem");
            helper.succeed();
        }
    
    static void creativeTabIsRegisteredWithForkIcon(GameTestHelper helper) {
            CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(ForbiddenMagic.id("forbidden"))
                .map(holder -> holder.value())
                .orElse(null);
            helper.assertTrue(tab != null, "the addon registers its own creative tab");
            helper.assertValueEqual(tab.getIconItem().getItem(), FmItems.DIABOLIST_FORK.get(), "the tab icon is the diabolist fork");
            helper.succeed();
        }
    
    static void creativeTabListsEveryRegisteredItem(GameTestHelper helper) {
            List<ItemStack> stacks = dev.visfox.forbiddenreforged.FmCreativeTab.stacks();
            java.util.Set<Item> listed = new java.util.HashSet<>();
            for (ItemStack stack : stacks) {
                listed.add(stack.getItem());
            }
            for (ItemStack stack : dev.visfox.forbiddenreforged.FmCreativeTab.mobCrystalStacks()) {
                listed.add(stack.getItem());
            }
            for (var holder : FmItems.ITEMS.getEntries()) {
                helper.assertTrue(listed.contains(holder.get()), "the tab is missing the item " + holder.getId());
            }
            for (var holder : dev.visfox.forbiddenreforged.FmBlocks.BLOCKS.getEntries()) {
                Item blockItem = holder.get().asItem();
                if (blockItem != Items.AIR) {
                    helper.assertTrue(listed.contains(blockItem), "the tab is missing the block " + holder.getId());
                }
            }
            long unique = stacks.stream().map(stack -> stack.getItem() + "#" + stack.getComponents()).distinct().count();
            helper.assertValueEqual(stacks.size(), (int) unique, "no duplicate entries in the tab list");
            helper.succeed();
        }
    
    static void orichalcumCapIsRegistered(GameTestHelper helper) {
            thaumcraft.wand.WandCap cap = WandRegistry.findCap("orichalcum");
            helper.assertTrue(cap != null, "the creative orichalcum cap is registered");
            helper.assertValueEqual(cap.baseCostModifier(), 0.0F, "orichalcum charges no vis");
            helper.assertValueEqual(cap.craftCost(), 1000, "orichalcum craft cost is 1000");
            helper.assertTrue(cap.specialAspects().isEmpty(), "orichalcum has no special aspects");
            helper.succeed();
        }
    
    static void diabolistForkSwitchesToTheSporkModel(GameTestHelper helper) {
            JsonObject definition = GameTestResources.readJson(helper, "/assets/forbiddenmagic/items/diabolist_fork.json");
            JsonObject model = definition.getAsJsonObject("model");
            helper.assertValueEqual(model.get("type").getAsString(), "minecraft:condition", "the fork model is conditional");
            helper.assertValueEqual(model.get("property").getAsString(), "forbiddenmagic:spork", "condition reads the spork config");
            helper.assertValueEqual(model.getAsJsonObject("on_true").get("model").getAsString(), "forbiddenmagic:item/diabolist_fork_spork", "on_true is the spork");
            helper.assertValueEqual(model.getAsJsonObject("on_false").get("model").getAsString(), "forbiddenmagic:item/diabolist_fork", "on_false is the ordinary fork");
            JsonObject spork = GameTestResources.readJson(helper, "/assets/forbiddenmagic/models/item/diabolist_fork_spork.json");
            helper.assertValueEqual(spork.getAsJsonObject("textures").get("layer0").getAsString(), "forbiddenmagic:item/spork", "spork model uses the spork texture");
            helper.assertTrue(ForbiddenMagic.class.getResourceAsStream("/assets/forbiddenmagic/textures/item/spork.png") != null, "spork texture ships with the mod");
            helper.succeed();
        }
    
    static void lustShardSwitchesToTheDespairModel(GameTestHelper helper) {
            JsonObject definition = GameTestResources.readJson(helper, "/assets/forbiddenmagic/items/lust_shard.json");
            JsonObject model = definition.getAsJsonObject("model");
            helper.assertValueEqual(model.get("type").getAsString(), "minecraft:condition", "the lust shard model is conditional");
            helper.assertValueEqual(model.get("property").getAsString(), "forbiddenmagic:no_lust", "condition reads the no_lust config");
            helper.assertValueEqual(model.getAsJsonObject("on_true").get("model").getAsString(), "forbiddenmagic:item/lust_shard_despair", "on_true is the despair shard");
            helper.assertValueEqual(model.getAsJsonObject("on_false").get("model").getAsString(), "forbiddenmagic:item/lust_shard", "on_false is the lust shard");
            JsonObject despair = GameTestResources.readJson(helper, "/assets/forbiddenmagic/models/item/lust_shard_despair.json");
            helper.assertValueEqual(despair.getAsJsonObject("textures").get("layer0").getAsString(), "forbiddenmagic:item/despair_shard", "despair model uses the despair texture");
            helper.assertTrue(ForbiddenMagic.class.getResourceAsStream("/assets/forbiddenmagic/textures/item/despair_shard.png") != null, "despair texture ships with the mod");
            helper.succeed();
        }
    
    static void ringOfNutritionOnlyWorksInRingSlots(GameTestHelper helper) {
            ServerPlayer player = GameTestPlayers.connected(helper);
            for (int slot = 0; slot < Baubles.slotCount(); slot++) {
                Baubles.set(player, slot, ItemStack.EMPTY);
            }
            ItemStack ring = new ItemStack(FmItems.RING_NUTRITION.get());
            helper.assertTrue(ring.getItem() instanceof Bauble, "Ring of Nutrition implements the host Bauble contract");
            helper.assertValueEqual(((Bauble) ring.getItem()).getBaubleType(ring), BaubleType.RING,
                "Ring of Nutrition registers as a ring, not an amulet or belt item");
            helper.assertTrue(ring.getItem() instanceof RunicArmor, "Ring of Nutrition is recognized by TC4 runic armor");
            helper.assertValueEqual(((RunicArmor) ring.getItem()).getRunicCharge(ring), 0,
                "Ring of Nutrition grants no runic charge");
            Baubles.set(player, 0, ring.copy());
            helper.assertFalse(FmToolEvents.hasNutritionRingInRingSlot(player), "amulet slot does not activate Ring of Nutrition");
            Baubles.set(player, 0, ItemStack.EMPTY);
            Baubles.set(player, 1, ring.copy());
            helper.assertTrue(FmToolEvents.hasNutritionRingInRingSlot(player), "first ring slot activates Ring of Nutrition");
            Baubles.set(player, 1, ItemStack.EMPTY);
            Baubles.set(player, 2, ring.copy());
            helper.assertTrue(FmToolEvents.hasNutritionRingInRingSlot(player), "second ring slot activates Ring of Nutrition");
            Baubles.set(player, 2, ItemStack.EMPTY);
            Baubles.set(player, 3, ring.copy());
            helper.assertFalse(FmToolEvents.hasNutritionRingInRingSlot(player), "belt slot does not activate Ring of Nutrition");
            helper.succeed();
        }
}
