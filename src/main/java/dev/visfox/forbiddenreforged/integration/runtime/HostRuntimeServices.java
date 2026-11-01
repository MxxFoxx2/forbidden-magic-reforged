package dev.visfox.forbiddenreforged.integration.runtime;

import dev.visfox.forbiddenreforged.WrathCageBlockEntity;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

/**
 * Minecraft-runtime capabilities supplied by the selected magic host.
 *
 * The contract uses Minecraft values because it is a runtime bridge, but it
 * deliberately does not expose any Thaumcraft or other host API types.
 */
public interface HostRuntimeServices {
    boolean isTaintedMob(LivingEntity entity);

    boolean isHostBoss(LivingEntity entity);

    boolean isHostGolem(LivingEntity entity);

    WrathCageBlockEntity createWrathCageBlockEntity(BlockPos pos, BlockState state);

    boolean isWand(ItemStack stack);

    String wandRodTag(ItemStack stack);

    int wandFocusTreasure(ItemStack stack);

    int wandFocusPotency(ItemStack stack);

    void addWandVis(ItemStack stack, ContentId aspect, int amount, boolean apply);

    void addRealVis(ItemStack stack, ContentId aspect, int amount, boolean apply);

    ItemStack accessory(Player player, int slot);

    boolean isTaintMaterial(BlockState state);

    ItemStack specialMiningDrop(BlockState state, ItemStack drop, float chance, RandomSource random);

    boolean isTaintDamage(DamageSource source);

    boolean isNoResearchDifficulty();

    List<ContentId> primalAspectIds();

    boolean addAspectPool(ServerPlayer player, ContentId aspect, int amount);
}
