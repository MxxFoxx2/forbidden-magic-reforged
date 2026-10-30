package dev.visfox.forbiddenreforged;

import com.leclowndu93150.thaumaturge.api.taint.ITaintBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class TaintShovelItem extends Item {
    private static final Identifier FLUX_GOO = Identifier.fromNamespaceAndPath("thaumaturge", "flux_goo");
    private static final Identifier FLUX_GAS = Identifier.fromNamespaceAndPath("thaumaturge", "flux_gas");

    public TaintShovelItem(Properties properties) {
        super(properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (isTaintMaterial(state)) {
            return FmMaterials.ELEMENTAL.speed();
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
                    if (!isFlux(state)) {
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
            level.playSound(null, center, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS,
                0.2F, 0.2F + level.getRandom().nextFloat() * 0.2F);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean isTaintMaterial(BlockState state) {
        return state.getBlock() instanceof ITaintBlock || isFlux(state);
    }

    private static boolean isFlux(BlockState state) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return id.equals(FLUX_GOO) || id.equals(FLUX_GAS);
    }
}
