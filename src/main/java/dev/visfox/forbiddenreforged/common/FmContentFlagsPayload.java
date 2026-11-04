package dev.visfox.forbiddenreforged.common;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server-to-client sync of the content-gating startup flags (no_lust,
 * emerald_transmutation, enchanting, greedy_ench, wrath_cage). STARTUP configs
 * are not synced by NeoForge, so the server sends the authoritative values on
 * login and disconnects clients whose registries would differ.
 */
public record FmContentFlagsPayload(boolean noLust, boolean emeraldTrans, boolean enchanting, boolean greedyEnch, boolean wrathCage)
    implements CustomPacketPayload {
    public static final Type<FmContentFlagsPayload> TYPE = new Type<>(FmIds.id("content_flags"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FmContentFlagsPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, FmContentFlagsPayload::noLust,
        ByteBufCodecs.BOOL, FmContentFlagsPayload::emeraldTrans,
        ByteBufCodecs.BOOL, FmContentFlagsPayload::enchanting,
        ByteBufCodecs.BOOL, FmContentFlagsPayload::greedyEnch,
        ByteBufCodecs.BOOL, FmContentFlagsPayload::wrathCage,
        FmContentFlagsPayload::new
    );

    public static FmContentFlagsPayload ofConfig() {
        return new FmContentFlagsPayload(
            FmConfig.lustDisabled(),
            FmConfig.emeraldTrans(),
            FmConfig.enchantmentsEnabled(),
            FmConfig.greedyEnchantmentEnabled(),
            FmConfig.wrathCage()
        );
    }

    public boolean matchesClient() {
        return noLust == FmConfig.lustDisabled()
            && emeraldTrans == FmConfig.emeraldTrans()
            && enchanting == FmConfig.enchantmentsEnabled()
            && greedyEnch == FmConfig.greedyEnchantmentEnabled()
            && wrathCage == FmConfig.wrathCage();
    }

    @Override
    public Type<FmContentFlagsPayload> type() {
        return TYPE;
    }
}
