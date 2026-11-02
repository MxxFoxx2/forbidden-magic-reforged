package dev.visfox.forbiddenreforged.core.block;

import java.util.ArrayList;
import java.util.List;
import dev.visfox.forbiddenreforged.core.model.ContentId;

/**
 * Host-independent three-channel storage, display selection, and persisted-state model
 * for the Wrath Cage. Physical Essentia transport and legacy key IO remain host-side.
 */
public final class WrathCageStorage {
    public static final int CAPACITY = 64;

    public enum Channel {
        MOB,
        WRATH,
        SLOTH
    }

    /**
     * Immutable semantic storage state. Values are deliberately not normalized so
     * legacy saved values round-trip without silently changing their meaning.
     */
    public record State(int mob, int wrath, int sloth, int mode) {
    }

    private int mob;
    private int wrath;
    private int sloth;
    private int mode;

    public int amount(Channel channel) {
        return switch (channel) {
            case MOB -> mob;
            case WRATH -> wrath;
            case SLOTH -> sloth;
        };
    }

    /**
     * Maps a semantic aspect identifier to its storage channel.
     * Optional Wrath/Sloth aspect IDs may be null when those aspects are unavailable.
     */
    public Channel channelFor(
        ContentId aspect,
        ContentId mobAspect,
        ContentId wrathAspect,
        ContentId slothAspect
    ) {
        if (aspect == null) {
            return null;
        }
        if (aspect.equals(mobAspect)) {
            return Channel.MOB;
        }
        if (wrathAspect != null && aspect.equals(wrathAspect)) {
            return Channel.WRATH;
        }
        if (slothAspect != null && aspect.equals(slothAspect)) {
            return Channel.SLOTH;
        }
        return null;
    }

    public void setAmount(Channel channel, int amount) {
        switch (channel) {
            case MOB -> mob = amount;
            case WRATH -> wrath = amount;
            case SLOTH -> sloth = amount;
        }
    }

    /** Captures the complete semantic state that belongs to this storage model. */
    public State snapshot() {
        return new State(mob, wrath, sloth, mode);
    }

    /** Restores a previously captured state without rewriting legacy values. */
    public void restore(State state) {
        java.util.Objects.requireNonNull(state, "state");
        mob = state.mob();
        wrath = state.wrath();
        sloth = state.sloth();
        mode = state.mode();
    }

    /** Adds only what fits and returns the accepted amount. */
    public int add(Channel channel, int requested) {
        int accepted = WrathCageBehavior.acceptedAmount(amount(channel), requested, CAPACITY);
        setAmount(channel, amount(channel) + accepted);
        return accepted;
    }

    /** Removes at most the available amount and returns what was removed. */
    public int remove(Channel channel, int requested) {
        int removed = Math.min(Math.max(0, requested), Math.max(0, amount(channel)));
        setAmount(channel, amount(channel) - removed);
        return removed;
    }

    public boolean contains(Channel channel, int requested) {
        return amount(channel) >= requested;
    }

    public boolean hasRoom() {
        return mob < CAPACITY || wrath < CAPACITY || sloth < CAPACITY;
    }

    /**
     * Returns eligible extraction channels in the legacy mob, Wrath, Sloth order.
     * The host performs each physical request until one source yields an aspect.
     */
    public List<Channel> extractionOrder(int cost) {
        if (cost <= 0) {
            return List.of();
        }
        List<Channel> order = new ArrayList<>(3);
        int mobStored = amount(Channel.MOB);
        int wrathStored = amount(Channel.WRATH);
        int slothStored = amount(Channel.SLOTH);
        if (WrathCageBehavior.canDrawMobEssentia(mobStored, CAPACITY)) {
            order.add(Channel.MOB);
        }
        if (WrathCageBehavior.canDrawWrathEssentia(mobStored, wrathStored, CAPACITY, cost)) {
            order.add(Channel.WRATH);
        }
        if (WrathCageBehavior.canDrawSlothEssentia(mobStored, wrathStored, slothStored, CAPACITY, cost)) {
            order.add(Channel.SLOTH);
        }
        return List.copyOf(order);
    }

    public int total() {
        return mob + wrath + sloth;
    }

    public Channel selectedChannel() {
        return switch (mode) {
            case 0 -> Channel.MOB;
            case 1 -> Channel.WRATH;
            default -> Channel.SLOTH;
        };
    }

    public int selectedAmount() {
        return amount(selectedChannel());
    }

    public int mode() {
        return mode;
    }

    public int cycleMode() {
        mode = mode >= 2 ? 0 : mode + 1;
        return mode;
    }

    /** Keeps legacy serialized mode values; unknown values select the Sloth channel. */
    public void setMode(int mode) {
        this.mode = mode;
    }
}
