package com.evandev.shadow_drop;

import com.evandev.shadow_drop.config.ModConfig;

public class CommonClass {
    public static boolean shouldRefresh = false;

    public static void init() {
        ModConfig.load();
    }
}