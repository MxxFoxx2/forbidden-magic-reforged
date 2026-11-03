package dev.visfox.forbiddenreforged.integration.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Host-dependent enchantment effects and damage-source classification. */
public interface HostEnchantmentEffectServices {
    ItemStack specialMiningDrop(BlockState state, ItemStack drop, float chance, RandomSource random);

    boolean isTaintDamage(DamageSource source);
}
