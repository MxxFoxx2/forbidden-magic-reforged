package dev.visfox.forbiddenreforged.test;

import dev.visfox.forbiddenreforged.core.wand.BlinkFocusBehavior;
import dev.visfox.forbiddenreforged.core.wand.HellfireWandBehavior;
import dev.visfox.forbiddenreforged.core.wand.WandTriggerAccess;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4BlinkFocusItem;
import thaumcraft.registry.ModBlocks;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.wand.WandPartsAccess;
import dev.visfox.forbiddenreforged.core.wand.WandVisAccess;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4WandPartsAdapter;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4WandVisAdapter;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4WandRuntimeAdapter;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4AspectAdapter;
import dev.visfox.forbiddenreforged.FmItems;
import dev.visfox.forbiddenreforged.FmCreativeTab;
import dev.visfox.forbiddenreforged.FmToolEvents;
import thaumcraft.arcane.FocalManipulatorBlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.node.EnergizedNodeBlockEntity;
import thaumcraft.research.ResearchCategories;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.WandCap;
import thaumcraft.wand.WandRod;
import thaumcraft.vis.VisNet;
import thaumcraft.wand.Wands;
import thaumcraft.wand.WandTriggers;
import thaumcraft.wand.focus.FocusItem;
import thaumcraft.wand.focus.FocusKeys;
import thaumcraft.wand.focus.FocusUpgradeType;

final class WandGameTests {
    private WandGameTests() {
    }

    static void wandRodsRegisterAsFunctionalCores(GameTestHelper helper) {
        WandRod tainted = WandRegistry.findRod("tainted");
        helper.assertTrue(tainted != null, "tainted rod must be registered in WandRegistry");
        helper.assertValueEqual(tainted.capacity(), 150, "tainted capacity");
        helper.assertValueEqual(tainted.craftCost(), 12, "tainted craft cost");
        helper.assertTrue(tainted.onUpdate() != null, "tainted rod has an update behavior");

        WandRod infernal = WandRegistry.findRod("infernal");
        helper.assertTrue(infernal != null && infernal.capacity() == 150, "infernal rod registered with capacity 150");
        helper.assertTrue(infernal.onUpdate() != null, "infernal rod has an update behavior");

        WandRod profane = WandRegistry.findRod("profane");
        helper.assertTrue(profane != null && profane.capacity() == 50, "profane rod registered with capacity 50");
        helper.assertTrue(profane.onUpdate() != null, "profane rod has an update behavior");

        helper.assertTrue(WandRegistry.findRod("profaned") != null, "profaned burnout rod registered");

        ItemStack wand = Wands.create(WandRegistry.CAP_THAUMIUM, tainted, false);
        helper.assertTrue(Wands.isWand(wand), "tainted wand is created");
        helper.assertValueEqual(Wands.getRod(wand).tag(), "tainted", "created wand uses the tainted rod");
        helper.succeed();
    }

    static void blinkFocusUpgradesUseOriginalIconAssets(GameTestHelper helper) {
        helper.assertValueEqual(Tc4BlinkFocusItem.HELLFIRE.icon(),
            ForbiddenMagic.id("textures/misc/hellfire.png"), "Hellfire uses the bundled original icon");
        helper.assertValueEqual(Tc4BlinkFocusItem.PANDEMONIUM.icon(),
            ForbiddenMagic.id("textures/misc/pandemonium.png"), "Pandemonium uses the bundled original icon");
        helper.assertTrue(ForbiddenMagic.class.getResourceAsStream("/assets/forbiddenmagic/textures/misc/hellfire.png") != null,
            "Hellfire icon asset is bundled");
        helper.assertTrue(ForbiddenMagic.class.getResourceAsStream("/assets/forbiddenmagic/textures/misc/pandemonium.png") != null,
            "Pandemonium icon asset is bundled");
        helper.succeed();
    }

    static void alchemicalCapAndEquivalentRodMatchOriginal(GameTestHelper helper) {
        WandCap cap = WandRegistry.findCap("alchemical");
        helper.assertTrue(cap != null, "alchemical wand cap is registered");
        if (cap != null) {
            helper.assertValueEqual(cap.baseCostModifier(), 0.9F, "alchemical cap base vis discount");
            helper.assertTrue(cap.specialAspects().contains(Aspect.WATER), "water has the special alchemical discount");
            helper.assertValueEqual(cap.getCostModifier(Aspect.WATER), 0.8F, "alchemical water discount");
            helper.assertValueEqual(cap.craftCost(), 7, "alchemical cap craft cost");
            helper.assertValueEqual(cap.item(), ForbiddenMagic.id("alchemical_cap"), "alchemical cap uses its component item");
            helper.assertValueEqual(cap.texture(), ForbiddenMagic.id("textures/model/wand_cap_alchemical.png"), "alchemical cap texture");
        }

        var equivalent = WandRegistry.findRod("equivalent");
        helper.assertTrue(equivalent != null, "equivalent wand rod is registered");
        if (equivalent != null) {
            helper.assertValueEqual(equivalent.capacity(), 100, "equivalent rod capacity");
            helper.assertValueEqual(equivalent.craftCost(), 12, "equivalent rod craft cost");
            helper.assertValueEqual(equivalent.item(), ForbiddenMagic.id("equivalent_rod"), "equivalent rod has a stable component item");
            helper.assertFalse(equivalent.staff(), "equivalent rod is not a staff");
            helper.assertFalse(equivalent.glowing(), "equivalent rod does not glow");
            helper.assertValueEqual(equivalent.texture(), ForbiddenMagic.id("textures/model/wand_rod_equivalent.png"), "equivalent rod texture");
        }

        helper.assertTrue(FmItems.ALCHEMICAL_CAP.isBound() && FmItems.EQUIVALENT_ROD.isBound(),
            "both wand component items are registered");
        helper.assertTrue(ForbiddenMagic.class.getResourceAsStream("/assets/forbiddenmagic/textures/model/wand_cap_alchemical.png") != null,
            "alchemical cap wand texture ships with the mod");
        helper.assertTrue(ForbiddenMagic.class.getResourceAsStream("/assets/forbiddenmagic/textures/model/wand_rod_equivalent.png") != null,
            "equivalent rod wand texture ships with the mod");
        ItemStack equivalentWand = equivalent == null ? ItemStack.EMPTY : Wands.create(WandRegistry.CAP_VOID, equivalent, false);
        helper.assertTrue(Wands.isWand(equivalentWand)
            && Wands.getCap(equivalentWand).tag().equals("void")
            && Wands.getRod(equivalentWand).tag().equals("equivalent"),
            "equivalent creative wand uses the void cap and equivalent rod");
        helper.assertTrue(FmCreativeTab.stacks().stream().anyMatch(stack -> Wands.isWand(stack)
            && Wands.getCap(stack).tag().equals("void") && Wands.getRod(stack).tag().equals("equivalent")),
            "equivalent wand is present in the creative tab");
        helper.succeed();
    }

    static void creativeWandFamilyMatchesOriginal(GameTestHelper helper) {
        WandRod neutronium = WandRegistry.findRod("neutronium");
        WandRod staff = WandRegistry.findRod("neutronium_staff");
        helper.assertTrue(neutronium != null, "neutronium rod is registered");
        helper.assertTrue(staff != null, "neutronium staff is registered");
        helper.assertValueEqual(neutronium.capacity(), 9001, "neutronium rod capacity");
        helper.assertValueEqual(staff.capacity(), 9002, "neutronium staff capacity");
        helper.assertTrue(neutronium.glowing() && staff.glowing(), "neutronium rod and staff glow");
        helper.assertTrue(!neutronium.staff() && staff.staff(), "only the second neutronium rod is a staff");
        helper.assertValueEqual(neutronium.craftCost(), 1000, "neutronium rod craft cost");
        helper.assertValueEqual(staff.craftCost(), 1000, "neutronium staff craft cost");
        helper.assertTrue(neutronium.onUpdate() != null && staff.onUpdate() != null, "creative rods refill vis on update");
        helper.assertValueEqual(neutronium.item(), Identifier.withDefaultNamespace("bedrock"), "creative rod uses bedrock");
        helper.assertValueEqual(staff.item(), Identifier.withDefaultNamespace("bedrock"), "creative staff uses bedrock");
        helper.assertTrue(ForbiddenMagic.class.getResourceAsStream("/assets/forbiddenmagic/textures/model/wand_rod_neutronium.png") != null, "neutronium rod texture ships with the mod");

        List<ItemStack> stacks = dev.visfox.forbiddenreforged.FmCreativeTab.stacks();
        helper.assertTrue(stacks.stream().anyMatch(stack -> Wands.isWand(stack)
            && Wands.getCap(stack).tag().equals("thaumium") && Wands.getRod(stack).tag().equals("tainted")), "tainted wand is in the creative tab");
        helper.assertTrue(stacks.stream().anyMatch(stack -> Wands.isWand(stack)
            && Wands.getCap(stack).tag().equals("thaumium") && Wands.getRod(stack).tag().equals("infernal")), "infernal wand is in the creative tab");
        helper.assertTrue(stacks.stream().anyMatch(stack -> Wands.isWand(stack)
            && Wands.getCap(stack).tag().equals("orichalcum") && Wands.getRod(stack).tag().equals("neutronium")), "creative neutronium wand is in the tab");
        helper.assertTrue(stacks.stream().anyMatch(stack -> Wands.isWand(stack)
            && Wands.getCap(stack).tag().equals("orichalcum") && Wands.getRod(stack).tag().equals("neutronium_staff")), "creative neutronium staff is in the tab");

        ItemStack neutroniumWand = stacks.stream().filter(stack -> Wands.isWand(stack)
            && Wands.getCap(stack).tag().equals("orichalcum") && Wands.getRod(stack).tag().equals("neutronium"))
            .findFirst().orElse(ItemStack.EMPTY);
        ItemStack staffWand = stacks.stream().filter(stack -> Wands.isWand(stack)
            && Wands.getCap(stack).tag().equals("orichalcum") && Wands.getRod(stack).tag().equals("neutronium_staff"))
            .findFirst().orElse(ItemStack.EMPTY);
        helper.assertFalse(neutroniumWand.isEmpty(), "creative neutronium wand can be located");
        helper.assertFalse(staffWand.isEmpty(), "creative neutronium staff can be located");
        var creativePlayer = helper.makeMockServerPlayer(net.minecraft.world.level.GameType.CREATIVE);
        Wands.getRod(neutroniumWand).onUpdate().onUpdate(neutroniumWand, creativePlayer);
        Wands.getRod(staffWand).onUpdate().onUpdate(staffWand, creativePlayer);
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            helper.assertValueEqual(Wands.getVis(neutroniumWand, aspect), Wands.getMaxVis(neutroniumWand), "neutronium rod fills " + aspect);
            helper.assertValueEqual(Wands.getVis(staffWand, aspect), Wands.getMaxVis(staffWand), "neutronium staff fills " + aspect);
        }

        ItemStack profaneWand = stacks.stream().filter(stack -> Wands.isWand(stack)
            && Wands.getCap(stack).tag().equals("iron") && Wands.getRod(stack).tag().equals("profane"))
            .findFirst().orElse(ItemStack.EMPTY);
        helper.assertFalse(profaneWand.isEmpty(), "pre-filled profane wand is in the creative tab");
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            helper.assertValueEqual(Wands.getVis(profaneWand, aspect), 5000, "profane creative wand starts with 5000 " + aspect);
        }
        helper.succeed();
    }

    static void wandTooltipKindsPreserveOriginalHints(GameTestHelper helper) {
        helper.assertValueEqual(FmToolEvents.wandTooltipKind("neutronium"), "creativeonly",
            "neutronium wand exposes the creative-only tooltip");
        helper.assertValueEqual(FmToolEvents.wandTooltipKind("neutronium_staff"), "creativeonly",
            "neutronium staff exposes the creative-only tooltip");
        helper.assertValueEqual(FmToolEvents.wandTooltipKind("profane"), "profane_contract",
            "profane wand exposes its contract tooltip");
        helper.assertValueEqual(FmToolEvents.wandTooltipKind("profaned"), "profane_contract",
            "profaned wand exposes the contract tooltip");
        helper.assertValueEqual(FmToolEvents.wandTooltipKind("greatwood"), "",
            "ordinary rods do not receive special wand tooltips");
        helper.assertValueEqual(FmToolEvents.wandTooltipKind(new ItemStack(Items.STICK)), "",
            "non-wand items do not receive wand-specific tooltips");

        WandRod neutronium = WandRegistry.findRod("neutronium");
        ItemStack neutroniumWand = Wands.create(WandRegistry.CAP_THAUMIUM, neutronium, false);
        helper.assertValueEqual(FmToolEvents.wandTooltipKind(neutroniumWand), "creativeonly",
            "neutronium wand stacks resolve their rod tag through the TC4 adapter");

        WandRod profane = WandRegistry.findRod("profane");
        ItemStack profaneWand = Wands.create(WandRegistry.CAP_THAUMIUM, profane, false);
        helper.assertValueEqual(FmToolEvents.wandTooltipKind(profaneWand), "profane_contract",
            "profane wand stacks resolve their rod tag through the TC4 adapter");

        WandRod tainted = WandRegistry.findRod("tainted");
        ItemStack taintedWand = Wands.create(WandRegistry.CAP_THAUMIUM, tainted, false);
        helper.assertValueEqual(FmToolEvents.wandTooltipKind(taintedWand), "",
            "ordinary registered wand stacks have no special tooltip");
        helper.succeed();
    }

    static void infernalAndTaintedWandDamageBehaviorsMatchOriginal(GameTestHelper helper) {
        WandRod tainted = WandRegistry.findRod("tainted");
        WandRod infernal = WandRegistry.findRod("infernal");
        helper.assertTrue(tainted != null && infernal != null, "both behavior-bearing rods must be registered");
        ItemStack taintedWand = Wands.create(WandRegistry.CAP_THAUMIUM, tainted, false);
        ItemStack infernalWand = Wands.create(WandRegistry.CAP_THAUMIUM, infernal, false);

        helper.assertTrue(FmToolEvents.cancelsInfernalDamage(infernalWand, true),
            "held Infernal Wand cancels fire or wither damage");
        helper.assertFalse(FmToolEvents.cancelsInfernalDamage(infernalWand, false),
            "Infernal Wand does not cancel unrelated damage");
        helper.assertFalse(FmToolEvents.cancelsInfernalDamage(taintedWand, true),
            "Tainted Wand does not gain Infernal immunity");

        helper.assertTrue(FmToolEvents.restoresTaintedVis(taintedWand, true),
            "held Tainted Wand reacts to the taint damage type");
        helper.assertFalse(FmToolEvents.restoresTaintedVis(taintedWand, false),
            "Tainted Wand does not react to non-taint damage");
        helper.assertFalse(FmToolEvents.restoresTaintedVis(infernalWand, true),
            "Infernal Wand does not use the Tainted Wand reaction");

        List<ContentId> restored = FmToolEvents.taintedVisRolls(RandomSource.create(42L));
        Set<ContentId> primalIds = Set.of(
            ContentId.parse("forbiddenmagic:concept/entropy"),
            ContentId.parse("forbiddenmagic:concept/order"),
            ContentId.parse("forbiddenmagic:concept/fire"),
            ContentId.parse("forbiddenmagic:concept/water"),
            ContentId.parse("forbiddenmagic:concept/earth"),
            ContentId.parse("forbiddenmagic:concept/air")
        );
        helper.assertValueEqual(restored.size(), 3, "taint damage makes three independent primal-aspect rolls");
        for (ContentId aspect : restored) {
            helper.assertTrue(primalIds.contains(aspect), "every restored aspect must use a semantic primal ID");
            helper.assertTrue(Tc4AspectAdapter.resolveAspect(aspect).isPrimal(),
                "the TC4 adapter must resolve every semantic ID to a primal aspect");
        }
        helper.succeed();
    }

    static void hellfireTriggerTargetsNetherrackAndObsidian(GameTestHelper helper) {
        helper.assertTrue(WandTriggers.hasTrigger(Blocks.NETHERRACK.defaultBlockState()),
            "Hellfire trigger must be registered for netherrack");
        helper.assertTrue(WandTriggers.hasTrigger(Blocks.OBSIDIAN.defaultBlockState()),
            "Hellfire trigger must be registered for obsidian");
        helper.assertTrue(ResearchCategories.getResearch("HELLFIRE") != null,
            "the trigger's HELLFIRE research gate must exist");
        helper.succeed();
    }

    static void hellfireBehaviorUsesHostNeutralAccess(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos position = new BlockPos(2, 3, 2);
        helper.setBlock(position, Blocks.OBSIDIAN);
        BlockPos absolutePosition = helper.absolutePos(position);
        ItemStack wand = new ItemStack(Items.STICK);
        boolean[] wandState = {true};
        boolean[] focusedState = {false};
        boolean[] researchState = {true};
        int[] consumed = {0};
        ContentId fire = ContentId.parse("forbiddenmagic:concept/fire");
        ContentId hellfire = ContentId.parse("forbiddenmagic:research/hellfire");
        WandTriggerAccess access = new WandTriggerAccess() {
            @Override
            public boolean isWand(ItemStack stack) {
                return wandState[0];
            }

            @Override
            public boolean hasFocus(ItemStack stack) {
                return focusedState[0];
            }

            @Override
            public boolean researchComplete(Player caster, ContentId research) {
                return researchState[0] && hellfire.equals(research);
            }

            @Override
            public boolean consumeVis(ItemStack stack, Player caster, ContentId aspect, int amount) {
                consumed[0]++;
                return fire.equals(aspect) && amount == 2;
            }
        };

        helper.assertFalse(HellfireWandBehavior.ignite(
            helper.getLevel(), wand, player, absolutePosition, Direction.NORTH, access
        ), "a side hit cannot trigger Hellfire");
        helper.assertValueEqual(consumed[0], 0, "an invalid side does not consume vis");

        wandState[0] = false;
        helper.assertFalse(HellfireWandBehavior.ignite(
            helper.getLevel(), wand, player, absolutePosition, Direction.UP, access
        ), "a non-wand item cannot trigger Hellfire");
        helper.assertValueEqual(consumed[0], 0, "a non-wand item does not consume vis");

        wandState[0] = true;
        focusedState[0] = true;
        helper.assertFalse(HellfireWandBehavior.ignite(
            helper.getLevel(), wand, player, absolutePosition, Direction.UP, access
        ), "a focused wand cannot trigger Hellfire");
        helper.assertValueEqual(consumed[0], 0, "a focused wand does not consume vis");

        focusedState[0] = false;
        researchState[0] = false;
        helper.assertFalse(HellfireWandBehavior.ignite(
            helper.getLevel(), wand, player, absolutePosition, Direction.UP, access
        ), "Hellfire research is required");
        helper.assertValueEqual(consumed[0], 0, "missing research does not consume vis");

        researchState[0] = true;
        helper.assertTrue(HellfireWandBehavior.ignite(
            helper.getLevel(), wand, player, absolutePosition, Direction.UP, access
        ), "an eligible wand can ignite fire");
        helper.assertValueEqual(consumed[0], 1, "successful Hellfire consumes vis exactly once");
        helper.assertTrue(helper.getLevel().getBlockState(absolutePosition.above()).is(Blocks.FIRE),
            "Hellfire places fire above the clicked block");
        helper.succeed();
    }

    static void blinkFocusHellfireAppliesThroughFocalManipulator(GameTestHelper helper) {
        BlockPos tablePos = new BlockPos(2, 2, 2);
        BlockPos nodePos = new BlockPos(5, 2, 2);
        helper.setBlock(tablePos, ModBlocks.FOCAL_MANIPULATOR.get());
        helper.setBlock(nodePos, ModBlocks.ENERGIZED_AURA_NODE.get());
        FocalManipulatorBlockEntity table = helper.getBlockEntity(tablePos, FocalManipulatorBlockEntity.class);
        EnergizedNodeBlockEntity node = helper.getBlockEntity(nodePos, EnergizedNodeBlockEntity.class);

        ItemStack focus = new ItemStack(FmItems.BLINK_FOCUS.get());
        FocusKeys.UPGRADES.set(focus, List.of(FocusUpgradeType.FRUGAL.id()));
        table.setItem(0, focus);

        node.getAuraBase().add(Aspect.FIRE, 4_000_000).add(Aspect.VOID, 400_000).add(Aspect.ENERGY, 400_000);
        node.setupNode();
        EnergizedNodeBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(nodePos), helper.getBlockState(nodePos), node);

        helper.assertTrue(node.getCurrentVis().getAmount(Aspect.FIRE) > 0, "test node is not storing fire vis");
        helper.assertTrue(VisNet.drainVis(helper.getLevel(), helper.absolutePos(tablePos), Aspect.FIRE, 1) > 0,
            "the focal manipulator is not wired to the test node's vis network");
        helper.assertTrue(node.getCurrentVis().getAmount(Aspect.FIRE) >= 800, "test node cannot supply the Hellfire cost");

        ServerPlayer player = GameTestPlayers.connected(helper);
        player.experienceLevel = 100;

        helper.assertTrue(table.startCraft(Tc4BlinkFocusItem.HELLFIRE.id(), player), "the manipulator refused the Hellfire upgrade");
        helper.assertValueEqual(table.getSize(), 2000, "Hellfire rank 2 pending vis");
        helper.assertValueEqual(table.getAspects().getAmount(Aspect.FIRE), 800, "Hellfire FIRE cost");
        helper.assertValueEqual(table.getAspects().getAmount(Aspect.AIR), 400, "Hellfire AIR cost");
        helper.assertValueEqual(table.getAspects().getAmount(Aspect.ENTROPY), 400, "Hellfire ENTROPY cost");
        helper.assertValueEqual(table.getAspects().getAmount(Aspect.ORDER), 400, "Hellfire ORDER cost");

        for (int tick = 0; tick < 400 && table.getSize() > 0; tick++) {
            FocalManipulatorBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(tablePos), helper.getBlockState(tablePos), table);
        }

        helper.assertValueEqual(table.getSize(), 0, "Hellfire craft never finished");
        FocusItem focusItem = (FocusItem) focus.getItem();
        helper.assertTrue(focusItem.isUpgradedWith(focus, Tc4BlinkFocusItem.HELLFIRE), "Hellfire was not stored on the focus");
        helper.succeed();
    }

    static void blinkFocusReachesBeyondWandReachAndLandsLikeOriginal(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos base = helper.absolutePos(new BlockPos(2, 3, 2));
        double px = base.getX() + 0.5;
        double py = 40.0;
        double pz = base.getZ() + 0.5;
        player.setPos(px, py, pz);
        player.setYRot(-90.0F);
        player.setYHeadRot(-90.0F);
        player.setXRot(0.0F);

        BlockPos targetBlock = BlockPos.containing(px + 20.0, py + 1.0, pz);
        helper.getLevel().setBlockAndUpdate(targetBlock, Blocks.STONE.defaultBlockState());
        helper.assertTrue(player.pick(4.5, 1.0F, false).getType() != HitResult.Type.BLOCK,
            "the test target must sit beyond the vanilla wand reach");

        HitResult hit = BlinkFocusBehavior.target(player);
        helper.assertTrue(hit instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK,
            "the blink focus must reach a block beyond the wand's own reach");
        BlockHitResult blockHit = (BlockHitResult) hit;
        helper.assertValueEqual(blockHit.getBlockPos(), targetBlock, "blink target block");

        Vec3 landing = BlinkFocusBehavior.destination(blockHit);
        helper.assertTrue(Math.abs(landing.x - (targetBlock.getX() - 0.5)) < 0.001, "blink lands half a block outside the west face");
        helper.assertTrue(Math.abs(landing.z - pz) < 0.001, "blink keeps the hit depth on Z");
        helper.assertTrue(Math.abs(landing.y - (py + 1.62)) < 0.01, "blink keeps the hit height on a side face");

        helper.getLevel().removeBlock(targetBlock, false);
        helper.succeed();
    }

    static void wandVisAndPartsUseNeutralAccessPorts(GameTestHelper helper) {
        WandRod tainted = WandRegistry.findRod("tainted");
        ItemStack wand = Wands.create(WandRegistry.CAP_THAUMIUM, tainted, false);
        WandVisAccess vis = new Tc4WandVisAdapter();
        WandPartsAccess parts = new Tc4WandPartsAdapter();
        ContentId fire = ContentId.parse("forbiddenmagic:concept/fire");

        helper.assertTrue(Tc4WandRuntimeAdapter.isWand(wand),
            "the runtime adapter recognizes registered wand stacks");
        helper.assertFalse(Tc4WandRuntimeAdapter.isWand(new ItemStack(Items.STICK)),
            "the runtime adapter rejects ordinary item stacks");
        helper.assertValueEqual(Tc4WandRuntimeAdapter.rodTag(wand), "tainted",
            "the runtime adapter exposes the registered rod tag");
        helper.assertValueEqual(Tc4WandRuntimeAdapter.focusTreasure(wand), Wands.getFocusTreasure(wand),
            "focus treasure reads are delegated without changing the host result");
        helper.assertValueEqual(Tc4WandRuntimeAdapter.focusPotency(wand), Wands.getFocusPotency(wand),
            "focus potency reads are delegated without changing the host result");

        helper.assertValueEqual(vis.maximumVis(wand), Wands.getMaxVis(wand),
            "neutral vis access reports host maximum capacity");
        helper.assertTrue(vis.primalAspects().contains(fire),
            "neutral vis access exposes the semantic fire aspect");
        vis.addVis(wand, fire, 2, true);
        helper.assertValueEqual(vis.vis(wand, fire), Wands.getVis(wand, Aspect.FIRE),
            "neutral vis access writes through to the TC4 vis store");
        helper.assertValueEqual(vis.vis(wand, fire), 200,
            "normal vis units retain the host's hundred-unit scale");
        vis.addRealVis(wand, fire, 75, true);
        helper.assertValueEqual(vis.vis(wand, fire), 275,
            "raw vis access preserves exact internal units");
        vis.fill(wand);
        for (ContentId aspect : vis.primalAspects()) {
            helper.assertValueEqual(vis.vis(wand, aspect), vis.maximumVis(wand),
                "neutral fill populates " + aspect);
        }

        parts.setRod(wand, ContentId.parse("forbiddenmagic:profaned"));
        helper.assertValueEqual(Wands.getRod(wand).tag(), "profaned",
            "neutral parts access changes the rod while preserving the other wand parts");
        helper.assertValueEqual(Wands.getCap(wand).tag(), "thaumium",
            "rod replacement preserves the existing cap");
        helper.succeed();
    }

    static void remainingWandCapsMatchOriginalCosts(GameTestHelper helper) {
        assertPlainWandCapCost(helper, "vinteum", 0.9F, 6);
        assertPlainWandCapCost(helper, "manasteel", 0.9F, 6);
        assertPlainWandCapCost(helper, "elementium", 0.8F, 9);
        assertPlainWandCapCost(helper, "terrasteel", 1.8F, 1);
        helper.succeed();
    }

        assertPlainWandCapCost(helper, "vinteum", 0.9F, 6);
        assertPlainWandCapCost(helper, "manasteel", 0.9F, 6);
        assertPlainWandCapCost(helper, "elementium", 0.8F, 9);
        assertPlainWandCapCost(helper, "terrasteel", 1.8F, 1);
        helper.succeed();
    }

    private static void assertPlainWandCapCost(GameTestHelper helper, String tag, float baseCostModifier, int craftCost) {
        WandCap cap = WandRegistry.findCap(tag);
        helper.assertTrue(cap != null, tag + " wand cap is registered");
        if (cap == null) {
            return;
        }
        helper.assertValueEqual(cap.tag(), tag, tag + " cap tag");
        helper.assertValueEqual(cap.baseCostModifier(), baseCostModifier, tag + " cap base vis cost modifier");
        helper.assertValueEqual(cap.craftCost(), craftCost, tag + " cap craft cost");
        helper.assertValueEqual(cap.item(), ForbiddenMagic.id(tag + "_cap"), tag + " cap component item");
        helper.assertValueEqual(cap.texture(), ForbiddenMagic.id("textures/model/wand_cap_" + tag + ".png"), tag + " cap texture");
        helper.assertTrue(cap.specialAspects().isEmpty(), tag + " cap has no special aspect discounts");
        helper.assertValueEqual(cap.specialCostModifier(), 0.0F, tag + " cap special cost modifier");
        helper.assertValueEqual(cap.getCostModifier(Aspect.FIRE), baseCostModifier, tag + " cap applies its base modifier to a plain aspect");
    }
}
