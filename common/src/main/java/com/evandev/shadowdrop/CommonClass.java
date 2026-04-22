package com.evandev.shadowdrop;

import com.evandev.shadowdrop.config.ModConfig;

public class CommonClass {
    public static boolean shouldRefresh = false;

    public static void init() {
        ModConfig.load();
    }
}