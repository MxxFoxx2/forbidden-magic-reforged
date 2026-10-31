package dev.visfox.forbiddenreforged;

import java.util.function.Supplier;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FmComponents {
    private static final DeferredRegister.DataComponents COMPONENTS =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ForbiddenMagic.MODID);
    public static final Supplier<DataComponentType<MorphState>> MORPH_STATE =
        COMPONENTS.registerComponentType("morph_state", builder -> builder
            .persistent(MorphState.CODEC)
            .networkSynchronized(MorphState.STREAM_CODEC));
    public static final Supplier<DataComponentType<String>> MOB_ID =
        COMPONENTS.registerComponentType("mob_id", builder -> builder
            .persistent(Codec.STRING)
            .networkSynchronized(ByteBufCodecs.STRING_UTF8));
    public static final Supplier<DataComponentType<Integer>> PROFANE_CONTRACT =
        COMPONENTS.registerComponentType("profane_contract", builder -> builder
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.INT));
    public static final Supplier<DataComponentType<String>> COLLAR_OWNER =
        COMPONENTS.registerComponentType("collar_owner", builder -> builder
            .persistent(Codec.STRING)
            .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    private FmComponents() {
    }

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
