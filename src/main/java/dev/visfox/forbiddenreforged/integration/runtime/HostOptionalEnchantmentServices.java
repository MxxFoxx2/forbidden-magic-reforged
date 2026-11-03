package dev.visfox.forbiddenreforged.integration.runtime;

/** Optional host/add-on bridge for registering enchantment infusion costs. */
public interface HostOptionalEnchantmentServices {
    void registerOptionalEnchantments();

    int registeredOptionalEnchantments();
}
