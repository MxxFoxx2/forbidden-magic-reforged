package dev.visfox.forbiddenreforged.core.wand;

/** Semantic classification of a wand rod, independent of a host registry tag. */
public enum WandRodKind {
    /** The inspected stack is not a recognized wand. */
    NONE,
    TAINTED,
    INFERNAL,
    PROFANE,
    NEUTRONIUM,
    /** A valid host wand rod with no Forbidden Magic-specific behavior. */
    OTHER
}
