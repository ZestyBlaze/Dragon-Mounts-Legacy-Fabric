package net.zestyblaze.dragonmounts;

import eu.midnightdust.lib.config.MidnightConfig;
import net.zestyblaze.dragonmounts.dragon.TameableDragon;

public class DMLConfig extends MidnightConfig {
    @Entry public static boolean ALLOW_EGG_OVERRIDE = true;
    public static boolean allowEggOverride() {
        return ALLOW_EGG_OVERRIDE;
    }

    @Entry public static boolean REPLENISH_EGGS = true;
    public static boolean replenishEggs() {
        return REPLENISH_EGGS;
    }

    @Entry public static boolean USE_LOOT_TABLES = false;
    public static boolean useLootTables() {
        return USE_LOOT_TABLES;
    }

    @Entry public static boolean UPDATE_HABITATS = true;
    public static boolean updateHabitats() {
        return UPDATE_HABITATS;
    }

    @Entry public static int REPRO_LIMIT = TameableDragon.DEFAULT_REPRO_LIMIT;
    public static int reproLimit() {
        return REPRO_LIMIT;
    }

    @Entry public static boolean CAMERA_FLIGHT = true;
    public static boolean cameraFlight() {
        return CAMERA_FLIGHT;
    }
}
