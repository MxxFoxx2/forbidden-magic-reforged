package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.wand.HellfireWandBehavior;
import dev.visfox.forbiddenreforged.core.wand.WandTriggerAccess;
import dev.visfox.forbiddenreforged.core.wand.WandTriggerCatalog;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import java.util.Set;
import thaumcraft.research.ResearchManager;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.WandTriggers;
import thaumcraft.wand.Wands;

public final class Tc4WandTriggerAdapter {
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
            return ResearchManager.isComplete(player, research.path().toUpperCase(java.util.Locale.ROOT));
        }

        @Override
        public boolean consumeVis(ItemStack wand, Player player, ContentId aspect, int amount) {
            return wand.getItem() instanceof WandItem wandItem
                && wandItem.consumeVis(wand, player, Tc4AspectAdapter.resolveAspect(aspect), amount, false);
        }
    };

    private Tc4WandTriggerAdapter() {
    }

    public static void register() {
        var definition = WandTriggerCatalog.hellfire();
        Set<net.minecraft.world.level.block.Block> blocks = definition.blocks().stream()
            .map(id -> BuiltInRegistries.BLOCK.get(
                ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path())))
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
        WandTriggers.register(
            state -> blocks.contains(state.getBlock()),
            (level, wand, player, position, side) ->
                HellfireWandBehavior.ignite(level, wand, player, position, side, ACCESS)
        );
    }
}
