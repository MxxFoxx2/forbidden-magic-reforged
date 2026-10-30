package dev.visfox.forbiddenreforged;

import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FmBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ForbiddenMagic.MODID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WrathCageBlockEntity>> WRATH_CAGE =
        BLOCK_ENTITIES.register("wrath_cage",
            () -> new BlockEntityType<>(WrathCageBlockEntity::new, Set.of(FmBlocks.WRATH_CAGE.get())));

    private FmBlockEntities() {
    }
}
