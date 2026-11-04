package dev.visfox.forbiddenreforged;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import thaumcraft.Config;
import thaumcraft.aspect.Aspect;
import thaumcraft.research.ResearchManager;

/** Restores player-side behavior for the two original Forbidden Magic scribing tools. */
public final class ScribeWellEvents {
    private static final ResourceLocation PRIMORDIAL_PEARL_ID = ResourceLocation.parse("thaumcraft:primordial_pearl");

    private ScribeWellEvents() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ScribeWellEvents::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(ScribeWellEvents::onItemCrafted);
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
            || Config.researchDifficulty() != -1
            || player.tickCount % 200 != 0) {
            return;
        }

        var inventory = player.getInventory();
        var primals = Aspect.getPrimalAspects();
        if (primals.isEmpty()) {
            return;
        }

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.is(FmItems.CRYSTALWELL.get())) {
                continue;
            }

            Aspect aspect = primals.get(player.getRandom().nextInt(primals.size()));
            ResearchManager.addAspectPool(player, aspect, player.getRandom().nextInt(4) + 1);
            int nextDamage = stack.getDamageValue() + 1;
            if (nextDamage >= stack.getMaxDamage()) {
                inventory.setItem(slot, ItemStack.EMPTY);
            } else {
                stack.setDamageValue(nextDamage);
            }
        }
    }

    private static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
            || player.level().isClientSide()
            || !event.getCrafting().is(FmItems.PRIMEWELL.get())) {
            return;
        }

        // The original Primewell crafting hook returns its primordial pearl.
        Item pearl = BuiltInRegistries.ITEM.getValue(PRIMORDIAL_PEARL_ID);
        if (pearl == Items.AIR) {
            ForbiddenMagic.LOGGER.warn("Could not refund Primewell's primordial pearl: {} is not registered", PRIMORDIAL_PEARL_ID);
            return;
        }

        // Match the original crafting hook: restore the pearl in the crafting matrix
        // so it is not consumed by the Primewell recipe.
        var matrix = event.getInventory();
        for (int slot = 0; slot < matrix.getContainerSize(); slot++) {
            ItemStack ingredient = matrix.getItem(slot);
            if (ingredient.is(pearl)) {
                ingredient.grow(1);
                matrix.setItem(slot, ingredient);
                return;
            }
        }

        // Some modern crafting paths may have already removed the matrix entry;
        // retain the item for the player in that case.
        ItemStack returnedPearl = new ItemStack(pearl);
        player.getInventory().add(returnedPearl);
        if (!returnedPearl.isEmpty()) {
            player.drop(returnedPearl, false, Prediction.PREDICTED);
        }
    }
}
