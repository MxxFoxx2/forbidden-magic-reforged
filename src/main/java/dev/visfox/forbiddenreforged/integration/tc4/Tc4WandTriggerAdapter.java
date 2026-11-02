package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.wand.HellfireWandBehavior;
import dev.visfox.forbiddenreforged.core.wand.WandTriggerAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import thaumcraft.aspect.Aspect;
import thaumcraft.research.ResearchManager;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.WandTriggers;
import thaumcraft.wand.Wands;

public final class Tc4WandTriggerAdapter {
    private static final ContentId FIRE = ContentId.parse("forbiddenmagic:concept/fire");
    private static final ContentId HELLFIRE_RESEARCH = ContentId.parse("forbiddenmagic:research/hellfire");
    private static final WandTriggerAccess ACCESS = new WandTriggerAccess() {
        @Override
        public boolean isWand(ItemStack wand) {
            return Wands.isWand(wand);
        }

        @Override
        public boolean hasFocus(ItemStack wand) {
            return Wands.getFocus(wand) != null;
        }

        @Override
        public boolean researchComplete(Player player, ContentId research) {
            return HELLFIRE_RESEARCH.equals(research) && ResearchManager.isComplete(player, "HELLFIRE");
        }

        @Override
        public boolean consumeVis(ItemStack wand, Player player, ContentId aspect, int amount) {
            return FIRE.equals(aspect)
                && wand.getItem() instanceof WandItem wandItem
                && wandItem.consumeVis(wand, player, Aspect.FIRE, amount, false);
        }
    };

    private Tc4WandTriggerAdapter() {
    }

    public static void register() {
        WandTriggers.register(
            state -> state.is(Blocks.OBSIDIAN) || state.is(Blocks.NETHERRACK),
            (level, wand, player, position, side) ->
                HellfireWandBehavior.ignite(level, wand, player, position, side, ACCESS)
        );
    }
}
