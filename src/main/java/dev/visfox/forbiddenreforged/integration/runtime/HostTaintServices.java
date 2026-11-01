package dev.visfox.forbiddenreforged.integration.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Host-specific taint materials, purification, drops, and taint damage. */
public interface HostTaintServices {
    boolean isTaintMaterial(BlockState state);

    boolean isTaintShovelPurificationTarget(BlockState state);

    float taintShovelMiningSpeed();

    void playTaintShovelSound(Level level, BlockPos pos);

    ItemStack specialMiningDrop(BlockState state, ItemStack drop, float chance, RandomSource random);

    boolean isTaintDamage(DamageSource source);
}
