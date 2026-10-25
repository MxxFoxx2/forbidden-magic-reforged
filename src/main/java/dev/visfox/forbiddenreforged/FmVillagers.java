package dev.visfox.forbiddenreforged;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import thaumcraft.aspect.Aspect;
import thaumcraft.registry.ModItems;
import thaumcraft.wand.WandCap;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.WandRod;
import thaumcraft.wand.Wands;

public final class FmVillagers {
    private static final DeferredRegister<VillagerProfession> PROFESSIONS =
        DeferredRegister.create(Registries.VILLAGER_PROFESSION, ForbiddenMagic.MODID);

    public static final DeferredHolder<VillagerProfession, VillagerProfession> HERETIC =
        PROFESSIONS.register("heretic", () -> new VillagerProfession(
            "heretic",
            holder -> false,
            holder -> false,
            ImmutableSet.of(),
            ImmutableSet.of(),
            SoundEvents.VILLAGER_WORK_CLERIC
        ));

    private FmVillagers() {
    }

    public static void register(IEventBus modEventBus) {
        PROFESSIONS.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(FmVillagers::onTrades);
    }

    private static void onTrades(VillagerTradesEvent event) {
        if (event.getType() != HERETIC.get()) {
            return;
        }

        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
        List<VillagerTrades.ItemListing> novice = trades.get(1);
        if (novice == null) {
            return;
        }

        novice.add(listing(random -> offer(
            Items.EMERALD, randomCount(random, 1, 2), null, 0,
            new ItemStack(FmItems.DEADLY_SHARDS.get("greed").get(), randomCount(random, 3, 4)), 7
        )));
        if (!FmConfig.lustDisabled()) {
            novice.add(listing(random -> offer(
                FmItems.DEADLY_SHARDS.get("lust").get(), randomCount(random, 2, 5), null, 0,
                new ItemStack(Items.EMERALD), 7
            )));
        }
        novice.add(listing(random -> offer(
            FmItems.DEADLY_SHARDS.get("wrath").get(), randomCount(random, 4, 9), null, 0,
            new ItemStack(Items.EMERALD), 7
        )));
        novice.add(listing(random -> offer(
            FmItems.GLUTTONY_SHARD.get(), randomCount(random, 4, 9), null, 0,
            new ItemStack(Items.EMERALD), 7
        )));
        novice.add(listing(random -> offer(
            Items.EMERALD, randomCount(random, 1, 3), null, 0,
            new ItemStack(Items.SKELETON_SKULL), 7
        )));
        novice.add(listing(random -> offer(
            Items.EMERALD, randomCount(random, 4, 7), null, 0,
            new ItemStack(Items.GHAST_TEAR), 7
        )));
        novice.add(listing(random -> offer(
            Items.EMERALD, 1, null, 0,
            new ItemStack(Items.GLOWSTONE_DUST, randomCount(random, 8, 16)), 7
        )));
        novice.add(listing(random -> {
            WandRod rod = WandRegistry.findRod("profane");
            if (rod == null) {
                return null;
            }
            ItemStack wand = Wands.create(WandRegistry.CAP_IRON, rod, false);
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                Wands.addVis(wand, aspect, 5000, true);
            }
            return offer(Items.EMERALD, randomCount(random, 8, 12), Items.STICK, 1, wand, 1);
        }));
        novice.add(listing(random -> offer(
            Items.EMERALD, randomCount(random, 4, 8), Items.DIAMOND, 1,
            new ItemStack(ModItems.FOCUS_FIRE.get()), 1
        )));
    }

    private static int randomCount(net.minecraft.util.RandomSource random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private static VillagerTrades.ItemListing listing(Function<net.minecraft.util.RandomSource, MerchantOffer> factory) {
        return (Entity trader, net.minecraft.util.RandomSource random) -> factory.apply(random);
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
