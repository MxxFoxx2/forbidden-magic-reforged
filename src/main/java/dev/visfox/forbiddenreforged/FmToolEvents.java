package dev.visfox.forbiddenreforged;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import thaumcraft.aspect.Aspect;
import thaumcraft.platform.Baubles;
import thaumcraft.wand.VisStorage;
import thaumcraft.wand.Wands;
import thaumcraft.registry.ModBlocks;
import thaumcraft.taint.TaintBlock;
import thaumcraft.taint.TaintFibresBlock;

public final class FmToolEvents {
    private static final Aspect[] TAINTED_PRIMALS = {
        Aspect.ENTROPY, Aspect.ORDER, Aspect.FIRE, Aspect.WATER, Aspect.EARTH, Aspect.AIR
    };

    private FmToolEvents() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(FmToolEvents::onBreakSpeed);
        NeoForge.EVENT_BUS.addListener(FmToolEvents::onBlockDrops);
        NeoForge.EVENT_BUS.addListener(FmToolEvents::onFoodFinished);
        NeoForge.EVENT_BUS.addListener(FmToolEvents::onTooltip);
    }

    public static float taintPickaxeSpeed(float hardness) {
        if (hardness == 0.0F) {
            return 0.0F;
        }
        if (hardness < 5.0F) {
            return 0.1F;
        }
        if (hardness < 20.0F) {
            return hardness / 2.0F;
        }
        return 5.0F + hardness;
    }

    public static boolean taintShardRoll(int roll, int fortune, boolean silkTouch) {
        int chance = 1 + 2 * fortune + (silkTouch ? 6 : 0);
        return roll <= chance;
    }

    public static void collarDamage(Player wearer, float amount, Entity attacker) {
        if (FmItems.SUB_COLLAR == null) {
            return;
        }
        ItemStack collar = Baubles.get(wearer, 0);
        if (!collar.is(FmItems.SUB_COLLAR.get())) {
            return;
        }
        int doses = 3 * (int) amount;
        RandomSource random = wearer.getRandom();
        if (attacker instanceof Player dominator) {
            int chance = 1;
            if (dominator.getMainHandItem().is(FmItems.RIDING_CROP.get())) {
                doses += 3;
                chance += 3;
            }
            if (wearer.level().dimension() == Level.NETHER && random.nextInt(30) < chance) {
                ItemEntity drop = new ItemEntity(wearer.level(), wearer.getX(), wearer.getY(), wearer.getZ(),
                    new ItemStack(FmItems.DEADLY_SHARDS.get("lust").get()));
                drop.setPickUpDelay(10);
                drop.setDeltaMovement(drop.getDeltaMovement().add(
                    random.nextFloat() * 0.05,
                    (random.nextFloat() - random.nextFloat()) * 0.1,
                    (random.nextFloat() - random.nextFloat()) * 0.1));
                wearer.level().addFreshEntity(drop);
            }
        }
        for (int dose = 0; dose < doses; dose++) {
            VisStorage.addRealVis(collar, TAINTED_PRIMALS[random.nextInt(TAINTED_PRIMALS.length)], 1, true);
        }
    }

    public static String wandTooltipKind(ItemStack stack) {
        return Wands.isWand(stack) ? wandTooltipKind(Wands.getRod(stack).tag()) : "";
    }

    public static String wandTooltipKind(String rodTag) {
        if (rodTag == null) {
            return "";
        }
        if (rodTag.startsWith("neutronium")) {
            return "creativeonly";
        }
        if (rodTag.startsWith("profane")) {
            return "profane_contract";
        }
        return "";
    }

    private static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!player.getMainHandItem().is(FmItems.TAINT_PICKAXE.get())) {
            return;
        }
        BlockPos pos = event.getPosition().orElse(BlockPos.ZERO);
        float hardness = event.getState().getDestroySpeed(player.level(), pos);
        event.setNewSpeed(taintPickaxeSpeed(hardness));
    }

    private static void onBlockDrops(BlockDropsEvent event) {
        Entity breaker = event.getBreaker();
        if (!(breaker instanceof ServerPlayer player) || breaker instanceof FakePlayer) {
            return;
        }
        ItemStack tool = event.getTool();
        BlockState state = event.getState();
        if (!tool.is(FmItems.TAINT_SHOVEL.get())
            || !event.getDrops().isEmpty()
            || !isTaintMaterial(state)
            || state.is(FmBlocks.TAINT_LEAVES.get())) {
            return;
        }

        int fortune = enchantmentLevel(player, tool, Enchantments.FORTUNE);
        boolean silkTouch = enchantmentLevel(player, tool, Enchantments.SILK_TOUCH) > 0;
        if (!taintShardRoll(event.getLevel().getRandom().nextInt(31), fortune, silkTouch)) {
            return;
        }
        BlockPos pos = event.getPos();
        ItemEntity drop = new ItemEntity(player.level(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            new ItemStack(FmItems.DEADLY_SHARDS.get("taint").get()));
        drop.setPickUpDelay(10);
        event.getDrops().add(drop);
    }

    public static boolean hasNutritionRingInRingSlot(Player player) {
        return hasNutritionRingInRingSlots(Baubles.get(player, 1), Baubles.get(player, 2));
    }

    public static boolean hasNutritionRingInRingSlots(ItemStack firstRingSlot, ItemStack secondRingSlot) {
        return firstRingSlot.is(FmItems.RING_NUTRITION.get())
            || secondRingSlot.is(FmItems.RING_NUTRITION.get());
    }

    public static boolean cancelsInfernalDamage(ItemStack held, boolean fireOrWither) {
        return fireOrWither && held != null && !held.isEmpty() && Wands.isWand(held)
            && "infernal".equals(Wands.getRod(held).tag());
    }

    public static boolean restoresTaintedVis(ItemStack held, boolean taintDamage) {
        return taintDamage && held != null && !held.isEmpty() && Wands.isWand(held)
            && "tainted".equals(Wands.getRod(held).tag());
    }

    public static List<Aspect> taintedVisRolls(RandomSource random) {
        return List.of(
            TAINTED_PRIMALS[random.nextInt(TAINTED_PRIMALS.length)],
            TAINTED_PRIMALS[random.nextInt(TAINTED_PRIMALS.length)],
            TAINTED_PRIMALS[random.nextInt(TAINTED_PRIMALS.length)]
        );
    }

    private static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        switch (wandTooltipKind(stack)) {
            case "creativeonly" -> event.getToolTip().add(Component.translatable("tooltip.creativeonly"));
            case "profane_contract" -> event.getToolTip().add(Component.translatable(
                "tooltip.profane_contract", stack.getOrDefault(FmComponents.PROFANE_CONTRACT.get(), 0)));
            default -> {
            }
        }
    }

    private static void onFoodFinished(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()
            || event.getItem().get(net.minecraft.core.component.DataComponents.FOOD) == null
            || !hasNutritionRingInRingSlot(player)) {
            return;
        }
        player.getFoodData().eat(2, 2.0F);
    }

    private static boolean isTaintMaterial(BlockState state) {
        return state.getBlock() instanceof TaintBlock
            || state.getBlock() instanceof TaintFibresBlock
            || state.is(ModBlocks.FLUX_GOO.get())
            || state.is(ModBlocks.FLUX_GAS.get())
            || state.is(FmBlocks.TAINT_LOG.get())
            || state.is(FmBlocks.TAINT_PLANKS.get())
            || state.is(FmBlocks.TAINT_STONE_SMOOTH.get())
            || state.is(FmBlocks.TAINT_STONE_BRICK.get());
    }

    private static int enchantmentLevel(ServerPlayer player, ItemStack stack, net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key) {
        return stack.getEnchantmentLevel(player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key));
    }
}
