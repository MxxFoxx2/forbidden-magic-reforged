package dev.visfox.forbiddenreforged.core.block;

/**
 * Host-independent three-channel storage and display selection for the Wrath Cage.
 * Physical Essentia transport and persistence remain in the host block entity.
 */
public final class WrathCageStorage {
    public static final int CAPACITY = 64;

    public enum Channel {
        MOB,
        WRATH,
        SLOTH
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

    public void setAmount(Channel channel, int amount) {
        switch (channel) {
            case MOB -> mob = amount;
            case WRATH -> wrath = amount;
            case SLOTH -> sloth = amount;
        }
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
