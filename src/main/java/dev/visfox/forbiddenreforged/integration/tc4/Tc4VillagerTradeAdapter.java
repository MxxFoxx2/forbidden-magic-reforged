package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.wand.CreativeWandCatalog;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import thaumcraft.registry.ModItems;

/** Owns the Thaumcraft-specific items in the heretic novice trade pool. */
public final class Tc4VillagerTradeAdapter {
    private Tc4VillagerTradeAdapter() {
    }

    public static void addNoviceTrades(List<VillagerTrades.ItemListing> trades) {
        trades.add(listing(random -> {
            ItemStack wand = Tc4WandAdapter.createCreativeWand(CreativeWandCatalog.profane());
            if (wand == null) {
                return null;
            }
            return offer(Items.EMERALD, randomCount(random, 8, 12), Items.STICK, 1, wand, 1);
        }));
        trades.add(listing(random -> offer(
            Items.EMERALD, randomCount(random, 4, 8), Items.DIAMOND, 1,
            new ItemStack(ModItems.FOCUS_FIRE.get()), 1
        )));
    }

    private static int randomCount(RandomSource random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private static VillagerTrades.ItemListing listing(Function<RandomSource, MerchantOffer> factory) {
        return (Entity trader, RandomSource random) -> factory.apply(random);
    }

    private static MerchantOffer offer(
        ItemLike first,
        int firstCount,
        ItemLike second,
        int secondCount,
        ItemStack result,
        int maxUses
    ) {
        Optional<ItemCost> secondCost = second == null
            ? Optional.empty()
            : Optional.of(new ItemCost(second, secondCount));
        return new MerchantOffer(
            new ItemCost(first, firstCount),
            secondCost,
            result,
            maxUses,
            0,
            0.0F
        );
    }
}
