package dev.visfox.forbiddenreforged.integration.tc4;

import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.damage.ModDamageTypes;
import thaumcraft.item.tool.SpecialMining;

public final class Tc4EnchantmentAdapter {
    private Tc4EnchantmentAdapter() {
    }

    public static ItemStack specialMiningDrop(BlockState state, ItemStack drop, float chance, RandomSource random) {
        return SpecialMining.find(state, drop, chance, random);
    }

    public static boolean isTaintDamage(DamageSource source) {
        return source.is(ModDamageTypes.TAINT);
    }
}
