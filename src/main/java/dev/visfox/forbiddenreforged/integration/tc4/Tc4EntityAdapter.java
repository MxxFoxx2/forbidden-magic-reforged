package dev.visfox.forbiddenreforged.integration.tc4;

import net.minecraft.world.entity.LivingEntity;
import thaumcraft.entity.TaintedMob;
import thaumcraft.entity.boss.ThaumcraftBoss;

public final class Tc4EntityAdapter {
    private Tc4EntityAdapter() {
    }

    public static boolean isTaintedMob(LivingEntity entity) {
        return entity instanceof TaintedMob;
    }

    public static boolean isHostBoss(LivingEntity entity) {
        return entity instanceof ThaumcraftBoss;
    }

    public static boolean isHostGolem(LivingEntity entity) {
        return entity instanceof thaumcraft.entity.golem.GolemBase;
    }
}
