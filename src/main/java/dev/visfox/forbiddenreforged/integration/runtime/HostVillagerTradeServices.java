package dev.visfox.forbiddenreforged.integration.runtime;

import java.util.List;
import net.minecraft.world.entity.npc.VillagerTrades;

/** Host-specific novice trades contributed to the Forbidden Magic heretic profession. */
public interface HostVillagerTradeServices {
    void addNoviceTrades(List<VillagerTrades.ItemListing> trades);
}
