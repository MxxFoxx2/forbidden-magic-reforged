package dev.visfox.forbiddenreforged.integration.runtime;

/** Host-native registration of wand materials and trigger callbacks. */
public interface HostWandRegistrationServices {
    void registerWands();

    void registerWandTriggers();

    boolean isArsMagicaLoaded();
}
