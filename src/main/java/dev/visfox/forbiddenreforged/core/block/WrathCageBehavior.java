package dev.visfox.forbiddenreforged.core.block;

/**
 * Host-independent Wrath Cage rules. Essentia extraction, entity spawning and
 * persistence remain in the Minecraft/TC4 block-entity integration.
 */
public final class WrathCageBehavior {
    public enum FuelChannel {
        SPECIAL,
        WRATH,
        SLOTH,
        NONE
    }

    private WrathCageBehavior() {
    }

    public static boolean shouldSpawn(
        boolean powered,
        boolean mobSet,
        int fuel,
        int cost,
        int availableEssentia
    ) {
        return !powered && mobSet && (cost == 0 || fuel > 0 || availableEssentia >= cost);
    }

    public static int nextDelay(int randomOffset, boolean slothful) {
        int delay = 200 + Math.max(0, Math.min(99, randomOffset));
        return slothful ? delay + 200 : delay;
    }

    public static boolean canDrawMobEssentia(int stored, int capacity) {
        return stored < capacity;
    }

    public static boolean canDrawWrathEssentia(int mobStored, int wrathStored, int capacity, int cost) {
        return wrathStored < capacity && mobStored < cost;
    }

    public static boolean canDrawSlothEssentia(
        int mobStored, int wrathStored, int slothStored, int capacity, int cost
    ) {
        return slothStored < capacity && mobStored < cost && wrathStored < cost;
    }

    /** Preserves the cage's legacy fuel priority: mob aspect, Wrath, then Sloth. */
    public static FuelChannel selectFuelChannel(int special, int wrath, int sloth, int cost) {
        if (cost <= 0) {
            return FuelChannel.NONE;
        }
        if (special >= cost) {
            return FuelChannel.SPECIAL;
        }
        if (wrath >= cost) {
            return FuelChannel.WRATH;
        }
        if (sloth >= cost) {
            return FuelChannel.SLOTH;
        }
        return FuelChannel.NONE;
    }

    /** Returns the portion of an incoming amount that fits in one essentia channel. */
    public static int acceptedAmount(int stored, int requested, int capacity) {
        if (requested <= 0 || capacity <= stored) {
            return 0;
        }
        return Math.min(requested, capacity - Math.max(0, stored));
    }
}
