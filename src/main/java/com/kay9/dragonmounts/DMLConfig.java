package com.kay9.dragonmounts;

import com.kay9.dragonmounts.dragon.TameableDragon;
import eu.midnightdust.lib.config.MidnightConfig;

public class DMLConfig {
    @MidnightConfig.Entry public static boolean ALLOW_EGG_OVERRIDE = true;
    @MidnightConfig.Entry public static boolean REPLENISH_EGGS = true;
    @MidnightConfig.Entry public static boolean USE_LOOT_TABLES = false;
    @MidnightConfig.Entry public static boolean UPDATE_HABITATS = true;
    @MidnightConfig.Entry(min = 0, max = Integer.MAX_VALUE) public static int REPRO_LIMIT = TameableDragon.DEFAULT_REPRO_LIMIT;
    @MidnightConfig.Entry public static boolean CAMERA_FLIGHT = true;
}
