package dev.visfox.forbiddenreforged.client;

import com.mojang.serialization.MapCodec;
import dev.visfox.forbiddenreforged.FmConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record NoLustCondition() implements ConditionalItemModelProperty {
    public static final MapCodec<NoLustCondition> MAP_CODEC = MapCodec.unit(new NoLustCondition());

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
        return FmConfig.lustDisabled();
    }

    @Override
    public MapCodec<NoLustCondition> type() {
        return MAP_CODEC;
    }
}
