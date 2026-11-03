package dev.visfox.forbiddenreforged;

import java.util.function.Supplier;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FmBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ForbiddenMagic.MODID);

    public static final Supplier<BlockEntityType<BlockEntity>> WRATH_CAGE =
        BLOCK_ENTITIES.register("wrath_cage", () ->
            BlockEntityType.Builder.of(
                (pos, state) -> HostAdapterRegistry.requireBlockEntities()
                    .createWrathCageBlockEntity(pos, state),
                FmBlocks.WRATH_CAGE.get()
            ).build(null));

    private FmBlockEntities() {
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
