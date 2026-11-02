package dev.visfox.forbiddenreforged;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;

public final class FmBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ForbiddenMagic.MODID);
    public static final Supplier<BlockEntityType<WrathCageBlockEntity>> WRATH_CAGE =
        BLOCK_ENTITIES.register("wrath_cage", () -> new BlockEntityType<>(
            HostAdapterRegistry.requireBlockEntities()::createWrathCageBlockEntity, FmBlocks.WRATH_CAGE.get()));

    private FmBlockEntities() {
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
