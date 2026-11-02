package dev.visfox.forbiddenreforged.core.block;

import java.util.Objects;

/**
 * Host-independent logical and persisted state for the Wrath Cage.
 * Minecraft/TC4 block entities remain responsible for translating this state
 * to and from their platform-specific persistence formats.
 */
public final class WrathCageState {
    public record Snapshot(
        String mobId,
        WrathCageStorage.State storage,
        int fuel,
        int spawnDelay,
        boolean slothful
    ) {
    }

    private final WrathCageStorage storage = new WrathCageStorage();
    private String mobId;
    private int fuel;
    private int spawnDelay = 20;
    private boolean slothful;

    public String mobId() {
        return mobId;
    }

    /**
     * Changing the selected mob clears only the mob-specific essentia channel,
     * matching the cage's legacy attunement behavior.
     */
    public void setMobId(String mobId) {
        if (!Objects.equals(this.mobId, mobId)) {
            storage.setAmount(WrathCageStorage.Channel.MOB, 0);
        }
        this.mobId = mobId;
    }

    public WrathCageStorage storage() {
        return storage;
    }

    public int fuel() {
        return fuel;
    }

    public void setFuel(int fuel) {
        this.fuel = fuel;
    }

    /** Consumes one active fuel tick without allowing negative live fuel. */
    public void consumeFuel() {
        if (fuel > 0) {
            fuel--;
        }
    }

    public int spawnDelay() {
        return spawnDelay;
    }

    public void setSpawnDelay(int spawnDelay) {
        this.spawnDelay = spawnDelay;
    }

    /**
     * Advances one active wait tick. Returns true when the caller must defer
     * spawning for this tick, preserving the legacy one-tick-at-zero boundary.
     */
    public boolean advanceSpawnDelay() {
        if (spawnDelay <= 0) {
            return false;
        }
        spawnDelay--;
        return true;
    }

    public boolean slothful() {
        return slothful;
    }

    public void setSlothful(boolean slothful) {
        this.slothful = slothful;
    }

    /** Evaluates the legacy spawn gate against this complete logical state. */
    public boolean canSpawn(boolean powered, int cost) {
        return WrathCageBehavior.shouldSpawn(powered, mobId != null, fuel, cost, storage.total());
    }

    /**
     * Selects and consumes one complete fuel channel in mob, Wrath, Sloth order.
     * Returns true only when a channel was consumed and a new fuel duration applied.
     */
    public boolean loadFuel(int cost, int fuelDuration) {
        if (cost <= 0 || fuel > 0) {
            return false;
        }

        WrathCageBehavior.FuelChannel channel = WrathCageBehavior.selectFuelChannel(
            storage.amount(WrathCageStorage.Channel.MOB),
            storage.amount(WrathCageStorage.Channel.WRATH),
            storage.amount(WrathCageStorage.Channel.SLOTH),
            cost
        );
        switch (channel) {
            case SPECIAL -> storage.remove(WrathCageStorage.Channel.MOB, cost);
            case WRATH -> storage.remove(WrathCageStorage.Channel.WRATH, cost);
            case SLOTH -> storage.remove(WrathCageStorage.Channel.SLOTH, cost);
            case NONE -> {
                return false;
            }
        }

        fuel = fuelDuration;
        slothful = channel == WrathCageBehavior.FuelChannel.SLOTH;
        return true;
    }

    public Snapshot snapshot() {
        return new Snapshot(mobId, storage.snapshot(), fuel, spawnDelay, slothful);
    }

    /** Restores the complete logical state without applying attunement side effects. */
    public void restore(Snapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        mobId = snapshot.mobId();
        storage.restore(snapshot.storage());
        fuel = snapshot.fuel();
        spawnDelay = snapshot.spawnDelay();
        slothful = snapshot.slothful();
    }
}
