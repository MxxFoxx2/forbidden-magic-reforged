package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.common.WrathCageBlockEntity;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.warp.WarpKind;
import dev.visfox.forbiddenreforged.integration.api.HostServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostAccessoryServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostAspectServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostBlockEntityServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostBootstrapServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostEnchantmentEffectServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostEntityAspectServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostEntityClassificationServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostItemFactoryServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostItemPropertyServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostMorphToolServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostScribingServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostTaintBlockServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWandInspectionServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWandVisServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWarpServices;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.EldritchContent;

/**
 * TC4 runtime ports, each backed by the focused adapter that owns that concern.
 * This is the only place that wires concrete TC4 services into the host-neutral
 * {@link HostServices} composition.
 */
public final class Tc4Services {
    public static final HostServices INSTANCE = new HostServices(
        new AspectPort(),
        new EntityAspectPort(),
        new EntityClassificationPort(),
        new MorphToolPort(),
        new BlockEntityPort(),
        new WandInspectionPort(),
        new WandVisPort(),
        new AccessoryPort(),
        new TaintBlockPort(),
        new EnchantmentEffectPort(),
        new ScribingPort(),
        new WarpPort(),
        new ItemPropertyPort(),
        new ItemFactoryPort(),
        new BootstrapPort()
    );

    private Tc4Services() {
    }

    private static final class AspectPort implements HostAspectServices {
        @Override
        public Component aspectDescription(ContentId aspect) {
            return Tc4AspectAdapter.description(aspect);
        }
    }

    private static final class EntityAspectPort implements HostEntityAspectServices {
        @Override
        public Map<ContentId, ContentId> aspectCatalog(boolean silverfishEmeralds) {
            return Tc4EntityAdapter.aspectCatalog(silverfishEmeralds);
        }
    }

    private static final class EntityClassificationPort implements HostEntityClassificationServices {
        @Override
        public boolean isTaintedMob(LivingEntity entity) {
            return Tc4EntityAdapter.isTaintedMob(entity);
        }

        @Override
        public boolean isHostBoss(LivingEntity entity) {
            return Tc4EntityAdapter.isHostBoss(entity);
        }

        @Override
        public boolean isHostGolem(LivingEntity entity) {
            return Tc4EntityAdapter.isHostGolem(entity);
        }

        @Override
        public Set<ContentId> bossEntityIds() {
            return Tc4EntityAdapter.bossEntityIds();
        }
    }

    private static final class WarpPort implements HostWarpServices {
        @Override
        public void changeWarp(ServerPlayer player, int amount, WarpKind kind) {
            Tc4WarpAdapter.changeWarp(player, amount, kind);
        }
    }

    private static final class MorphToolPort implements HostMorphToolServices {
        @Override
        public void playMorphCycleSound(Level level, BlockPos pos, RandomSource random) {
            Tc4MorphToolAdapter.playCycleSound(level, pos, random);
        }

        @Override
        public boolean isAreaHarvesting() {
            return Tc4MorphToolAdapter.isAreaHarvesting();
        }

        @Override
        public void breakArea(
            ItemStack tool,
            ServerLevel level,
            ServerPlayer player,
            BlockPos origin,
            Predicate<BlockState> correctTool
        ) {
            Tc4MorphToolAdapter.breakArea(tool, level, player, origin, correctTool);
        }
    }

    private static final class BlockEntityPort implements HostBlockEntityServices {
        @Override
        public WrathCageBlockEntity createWrathCageBlockEntity(BlockPos pos, BlockState state) {
            return new Tc4WrathCageBlockEntity(pos, state);
        }
    }

    private static final class WandInspectionPort implements HostWandInspectionServices {
        @Override
        public boolean isWand(ItemStack stack) {
            return Tc4WandRuntimeAdapter.isWand(stack);
        }

        @Override
        public String wandRodTag(ItemStack stack) {
            return Tc4WandRuntimeAdapter.rodTag(stack);
        }

        @Override
        public int wandFocusTreasure(ItemStack stack) {
            return Tc4WandRuntimeAdapter.focusTreasure(stack);
        }

        @Override
        public int wandFocusPotency(ItemStack stack) {
            return Tc4WandRuntimeAdapter.focusPotency(stack);
        }
    }

    private static final class WandVisPort implements HostWandVisServices {
        @Override
        public void addWandVis(ItemStack stack, ContentId aspect, int amount, boolean apply) {
            Tc4WandRuntimeAdapter.addVis(stack, aspect, amount, apply);
        }

        @Override
        public void addRealVis(ItemStack stack, ContentId aspect, int amount, boolean apply) {
            Tc4VisAdapter.addRealVis(stack, aspect, amount, apply);
        }
    }

    private static final class AccessoryPort implements HostAccessoryServices {
        @Override
        public ItemStack accessory(Player player, int slot) {
            return Tc4AccessoryAdapter.getBauble(player, slot);
        }
    }

    private static final class TaintBlockPort implements HostTaintBlockServices {
        @Override
        public boolean isTaintMaterial(BlockState state) {
            return Tc4TaintAdapter.isHostTaintMaterial(state);
        }

        @Override
        public boolean isTaintShovelPurificationTarget(BlockState state) {
            return Tc4TaintAdapter.isTaintShovelPurificationTarget(state);
        }

        @Override
        public float taintShovelMiningSpeed() {
            return Tc4TaintAdapter.taintShovelMiningSpeed();
        }

        @Override
        public void playTaintShovelSound(Level level, BlockPos pos) {
            Tc4TaintAdapter.playTaintShovelSound(level, pos);
        }
    }

    private static final class EnchantmentEffectPort implements HostEnchantmentEffectServices {
        @Override
        public ItemStack specialMiningDrop(BlockState state, ItemStack drop, float chance, RandomSource random) {
            return Tc4EnchantmentAdapter.specialMiningDrop(state, drop, chance, random);
        }

        @Override
        public boolean isTaintDamage(DamageSource source) {
            return Tc4EnchantmentAdapter.isTaintDamage(source);
        }
    }

    private static final class ScribingPort implements HostScribingServices {
        @Override
        public boolean isNoResearchDifficulty() {
            return Tc4ScribingAdapter.isNoResearchDifficulty();
        }

        @Override
        public List<ContentId> primalAspectIds() {
            return Tc4ScribingAdapter.primalAspectIds();
        }

        @Override
        public boolean addAspectPool(ServerPlayer player, ContentId aspect, int amount) {
            return Tc4ScribingAdapter.addAspectPool(player, aspect, amount);
        }
    }

    private static final class ItemPropertyPort implements HostItemPropertyServices {
        @Override
        public Item.Properties gluttonyFoodProperties(Item.Properties properties) {
            return Tc4ItemRegistrationAdapter.gluttonyFood(properties);
        }

        @Override
        public Item.Properties skullAxeProperties(Item.Properties properties) {
            return Tc4ItemRegistrationAdapter.skullAxe(properties);
        }

        @Override
        public Item.Properties morphPickaxeProperties(Item.Properties properties) {
            return Tc4ItemRegistrationAdapter.morphPickaxe(properties);
        }

        @Override
        public Item.Properties morphSwordProperties(Item.Properties properties) {
            return Tc4ItemRegistrationAdapter.morphSword(properties);
        }

        @Override
        public Item.Properties morphShovelProperties(Item.Properties properties) {
            return Tc4ItemRegistrationAdapter.morphShovel(properties);
        }

        @Override
        public Item.Properties morphAxeProperties(Item.Properties properties) {
            return Tc4ItemRegistrationAdapter.morphAxe(properties);
        }

        @Override
        public Item.Properties diabolistForkProperties(Item.Properties properties) {
            return Tc4ItemRegistrationAdapter.diabolistFork(properties);
        }

        @Override
        public Item.Properties taintShovelProperties(Item.Properties properties) {
            return Tc4ItemRegistrationAdapter.taintShovel(properties);
        }

        @Override
        public Item.Properties taintPickaxeProperties(Item.Properties properties) {
            return Tc4ItemRegistrationAdapter.taintPickaxe(properties);
        }

        @Override
        public Item.Properties blinkFocusProperties(Item.Properties properties) {
            return Tc4BlinkFocusItem.properties(properties);
        }
    }

    private static final class ItemFactoryPort implements HostItemFactoryServices {
        @Override
        public Item createMorphTool(Item.Properties properties) {
            return new Tc4MorphToolItem(properties);
        }

        @Override
        public Item createRingNutrition(Item.Properties properties) {
            return new Tc4RingNutritionItem(properties);
        }

        @Override
        public Item createCrystalwell(Item.Properties properties) {
            return new Tc4CrystalwellItem(properties);
        }

        @Override
        public Item createPrimewell(Item.Properties properties) {
            return new Tc4PrimewellItem(properties);
        }

        @Override
        public Item createBlinkFocus(Item.Properties properties) {
            return new Tc4BlinkFocusItem(properties);
        }

        @Override
        public Item createSubCollar(Item.Properties properties) {
            return new Tc4SubCollarItem(properties);
        }

        @Override
        public Item primordialPearlItem() {
            return EldritchContent.PRIMORDIAL_PEARL.get();
        }
    }

    private static final class BootstrapPort implements HostBootstrapServices {
        @Override
        public void registerWands() {
            Tc4WandAdapter.registerWands();
        }

        @Override
        public void registerWandTriggers() {
            Tc4WandTriggerAdapter.register();
        }

        @Override
        public void registerOptionalEnchantments() {
            Tc4TinkererAdapter.register();
        }

        @Override
        public int registeredOptionalEnchantments() {
            return Tc4TinkererAdapter.registeredCount();
        }

        @Override
        public void addCreativeWands(List<ItemStack> stacks) {
            Tc4WandAdapter.addCreativeWands(stacks);
        }
    }
}
