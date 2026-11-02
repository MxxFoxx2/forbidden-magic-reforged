package dev.visfox.forbiddenreforged.core.block;

/**
 * Host-independent Wrath Cage rules. Essentia extraction, entity spawning and
 * persistence remain in the Minecraft/TC4 block-entity integration.
 */
public final class WrathCageBehavior {
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

    /** Returns the portion of an incoming amount that fits in one essentia channel. */
    public static int acceptedAmount(int stored, int requested, int capacity) {
        if (requested <= 0 || capacity <= stored) {
            return 0;
        }
        return Math.min(requested, capacity - Math.max(0, stored));
    }
}
