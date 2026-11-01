package dev.visfox.forbiddenreforged;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4TaintAdapter;

public final class TaintShovelItem extends Item {
    public TaintShovelItem(Properties properties) {
        super(properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (Tc4TaintAdapter.isHostTaintMaterial(state) || state.is(FmBlocks.TAINT_LEAVES.get())) {
            return Tc4TaintAdapter.taintShovelMiningSpeed();
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos center = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        int purified = 0;

        for (int x = center.getX() - 5; x <= center.getX() + 5; x++) {
            for (int y = center.getY() - 4; y <= center.getY() + 4; y++) {
                for (int z = center.getZ() - 5; z <= center.getZ() + 5; z++) {
                    BlockPos target = new BlockPos(x, y, z);
                    if (!level.hasChunkAt(target)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(target);
                    if (!Tc4TaintAdapter.isTaintShovelPurificationTarget(state)) {
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
            stack.hurtAndBreak(Math.min(purified, 15), player, context.getHand());
            Tc4TaintAdapter.playTaintShovelSound(level, center);
        }
        return InteractionResult.SUCCESS;
    }

}
