package dev.visfox.forbiddenreforged;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.Config;
import thaumcraft.aspect.Aspect;
import thaumcraft.item.ScribingToolsItem;
import thaumcraft.registry.ModItems;
import thaumcraft.research.ResearchManager;

/**
 * Original Forbidden Magic Crystalwell: a finite scribing tool that either
 * generates a small amount of primal research knowledge in easy mode or
 * converts into a spent inkwell after yielding a full set of primal aspects.
 */
public final class CrystalwellItem extends ScribingToolsItem {
    public CrystalwellItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return Config.researchDifficulty() == -1 ? 50 : 100;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (Config.researchDifficulty() == -1 || stack.getDamageValue() < stack.getMaxDamage()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        for (Aspect aspect : Aspect.getPrimalAspects()) {
            ResearchManager.addAspectPool(serverPlayer, aspect, 4 + level.getRandom().nextInt(4));
        }

        ItemStack spentInkwell = new ItemStack(ModItems.SCRIBING_TOOLS.get());
        spentInkwell.setDamageValue(spentInkwell.getMaxDamage());
        player.setItemInHand(hand, spentInkwell);
        return InteractionResult.SUCCESS;
    }
}
