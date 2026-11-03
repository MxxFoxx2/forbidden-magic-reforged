package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class TaintShovelItem extends Item {
    public TaintShovelItem(Properties properties) {
        super(properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        var host = HostAdapterRegistry.requireTaintBlocks();
        if (host.isTaintMaterial(state) || state.is(FmBlocks.TAINT_LEAVES.get())) {
            return host.taintShovelMiningSpeed();
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos center = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        var host = HostAdapterRegistry.requireTaintBlocks();
        int purified = 0;

        for (int x = center.getX() - 5; x <= center.getX() + 5; x++) {
            for (int y = center.getY() - 4; y <= center.getY() + 4; y++) {
                for (int z = center.getZ() - 5; z <= center.getZ() + 5; z++) {
                    BlockPos target = new BlockPos(x, y, z);
                    if (!level.hasChunkAt(target)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(target);
                    if (!host.isTaintShovelPurificationTarget(state)) {
                        continue;
                    }
                    purified++;
                    if (!level.isClientSide()) {
                        level.removeBlock(target, false);
                    }
                }
            }
        }

        if (purified == 0) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && player != null) {
            stack.hurtAndBreak(Math.min(purified, 15), player,
                context.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                    ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            host.playTaintShovelSound(level, center);
        }
        return InteractionResult.SUCCESS;
    }
}
