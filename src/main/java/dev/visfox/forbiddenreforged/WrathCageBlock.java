package dev.visfox.forbiddenreforged;

import javax.annotation.Nullable;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class WrathCageBlock extends Block implements EntityBlock {
    public WrathCageBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return HostAdapterRegistry.requireBlockEntities().createWrathCageBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
        Level level, BlockState state, BlockEntityType<T> type
    ) {
        if (type != FmBlockEntities.WRATH_CAGE.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) ->
            HostAdapterRegistry.requireBlockEntities().tickWrathCage(tickLevel, pos, tickState, entity);
    }

    @Override
    protected InteractionResult useWithoutItem(
        BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit
    ) {
        BlockEntity cage = level.getBlockEntity(pos);
        if (cage == null || !attune(cage, player.getMainHandItem())) {
            return InteractionResult.PASS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected ItemInteractionResult useItemOn(
        ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
        InteractionHand hand, BlockHitResult hit
    ) {
        BlockEntity cage = level.getBlockEntity(pos);
        if (cage == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (attune(cage, stack)) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!stack.is(FmItems.DIABOLIST_FORK.get()) || FmConfig.wrathCost() <= 0) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide()) {
            HostAdapterRegistry.requireBlockEntities().cycleWrathCageMode(cage);
        }
        player.swing(hand);
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        HostAdapterRegistry.requireBlockEntities().beforeRemoveWrathCage(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public static boolean attune(BlockEntity cage, ItemStack held) {
        String incoming = MobCrystalItem.mob(held);
        if (incoming == null || !FmSpawnerMobs.ids().contains(incoming)) {
            return false;
        }
        var host = HostAdapterRegistry.requireBlockEntities();
        String previous = host.wrathCageMobId(cage);
        host.setWrathCageMobId(cage, incoming);
        if (previous == null) {
            held.remove(FmComponents.MOB_ID.get());
        } else {
            MobCrystalItem.setMob(held, previous);
        }
        return true;
    }

    public static ItemStack crystalForBreak(BlockEntity cage) {
        ItemStack stack = new ItemStack(FmItems.MOB_CRYSTAL.get());
        String mobId = HostAdapterRegistry.requireBlockEntities().wrathCageMobId(cage);
        if (mobId != null) {
            MobCrystalItem.setMob(stack, mobId);
        }
        return stack;
    }
}
