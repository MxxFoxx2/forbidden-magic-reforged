package dev.visfox.forbiddenreforged.client;

import com.mojang.serialization.MapCodec;
import dev.visfox.forbiddenreforged.FmComponents;
import dev.visfox.forbiddenreforged.MorphState;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record MorphPhaseTintSource() implements ItemTintSource {
    public static final MapCodec<MorphPhaseTintSource> MAP_CODEC = MapCodec.unit(new MorphPhaseTintSource());

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        return stack.getOrDefault(FmComponents.MORPH_STATE.get(), MorphState.EMPTY).tintColor();
    }

    @Override
    public MapCodec<MorphPhaseTintSource> type() {
        return MAP_CODEC;
    }
}
