package net.terrunic.shadowdrop;

import net.terrunic.shadowdrop.config.ModConfig;

public class CommonClass {
    public static boolean shouldRefresh = false;

    public static void init() {
        ModConfig.load();
    }
}