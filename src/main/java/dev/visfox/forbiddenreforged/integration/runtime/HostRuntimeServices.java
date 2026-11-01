package dev.visfox.forbiddenreforged.integration.runtime;

import dev.visfox.forbiddenreforged.WrathCageBlockEntity;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
import java.util.function.Predicate;

/**
 * Minecraft-runtime capabilities supplied by the selected magic host.
 *
 * The contract uses Minecraft values because it is a runtime bridge, but it
 * deliberately does not expose any Thaumcraft or other host API types.
 */
public interface HostRuntimeServices {
    Component aspectDescription(ContentId aspect);

    void playMorphCycleSound(Level level, BlockPos pos, RandomSource random);

    boolean isAreaHarvesting();

    void breakArea(
        ItemStack tool,
        ServerLevel level,
        ServerPlayer player,
        BlockPos origin,
        Predicate<BlockState> correctTool
    );

    WrathCageBlockEntity createWrathCageBlockEntity(BlockPos pos, BlockState state);

    boolean isWand(ItemStack stack);

    String wandRodTag(ItemStack stack);

    int wandFocusTreasure(ItemStack stack);

    int wandFocusPotency(ItemStack stack);

    void addWandVis(ItemStack stack, ContentId aspect, int amount, boolean apply);

    void addRealVis(ItemStack stack, ContentId aspect, int amount, boolean apply);

    ItemStack accessory(Player player, int slot);

    boolean isTaintMaterial(BlockState state);

    boolean isTaintShovelPurificationTarget(BlockState state);

    float taintShovelMiningSpeed();

    void playTaintShovelSound(Level level, BlockPos pos);

    ItemStack specialMiningDrop(BlockState state, ItemStack drop, float chance, RandomSource random);

    boolean isTaintDamage(DamageSource source);

    boolean isNoResearchDifficulty();

    List<ContentId> primalAspectIds();

    boolean addAspectPool(ServerPlayer player, ContentId aspect, int amount);
}
