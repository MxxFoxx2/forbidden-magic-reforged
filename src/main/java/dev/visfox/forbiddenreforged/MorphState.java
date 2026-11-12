package dev.visfox.forbiddenreforged;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record MorphState(int phase, List<ItemEnchantments> sets, List<String> names) {
    public static final int PHASES = 3;
    public static final MorphState EMPTY = new MorphState(
        0,
        List.of(ItemEnchantments.EMPTY, ItemEnchantments.EMPTY, ItemEnchantments.EMPTY),
        List.of("", "", "")
    );
    public static final Codec<MorphState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.intRange(0, PHASES - 1).fieldOf("phase").forGetter(MorphState::phase),
        ItemEnchantments.CODEC.listOf(PHASES, PHASES).fieldOf("sets").forGetter(MorphState::sets),
        Codec.STRING.listOf(PHASES, PHASES).fieldOf("names").forGetter(MorphState::names)
    ).apply(instance, MorphState::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MorphState> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        MorphState::phase,
        ItemEnchantments.STREAM_CODEC.apply(ByteBufCodecs.list(PHASES)),
        MorphState::sets,
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(PHASES)),
        MorphState::names,
        MorphState::new
    );

    public MorphState {
        sets = List.copyOf(sets);
        names = List.copyOf(names);
        if (sets.size() != PHASES || names.size() != PHASES || phase < 0 || phase >= PHASES) {
            throw new IllegalArgumentException("morph state needs three slots and a phase in range");
        }
    }

    public MorphState store(ItemEnchantments set, String name) {
        ItemEnchantments[] nextSets = sets.toArray(ItemEnchantments[]::new);
        String[] nextNames = names.toArray(String[]::new);
        nextSets[phase] = set;
        nextNames[phase] = name;
        return new MorphState(phase, List.of(nextSets), List.of(nextNames));
    }

    public MorphState advance() {
        return new MorphState((phase + 1) % PHASES, sets, names);
    }

    public int tintColor() {
        return switch (phase) {
            case 1 -> 0xFF0010CC;
            case 2 -> 0xFFE5DA00;
            default -> 0xFF980000;
        };
    }

    public ItemEnchantments currentSet() {
        return sets.get(phase);
    }

    public String currentName() {
        return names.get(phase);
    }
}
