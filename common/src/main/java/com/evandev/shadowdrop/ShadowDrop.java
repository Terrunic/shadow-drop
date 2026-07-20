package com.evandev.shadowdrop;

import com.evandev.shadowdrop.compat.EmiCompat;
import com.evandev.shadowdrop.platform.Services;
import net.minecraft.world.item.ItemStack;

public class ShadowDrop {
    public static final String MOD_ID = "shadowdrop";

    // Bool to force refresh of corner pixel tests for drop shadows
    public static boolean shouldRefresh = false;
    // Tracker for item currently under cursor
    public static ItemStack hoveredItem = ItemStack.EMPTY;
    // Tracker for current active GuiGraphics
    public static Object currentGuiGraphics = null;
    // Tracker for GUI item render depth
    public static int guiRenderDepth = 0;

    public static void init() {
        ShadowDropConfig.load();

        if (Services.PLATFORM.isModLoaded("emi")) {
            try {
                EmiCompat.init();
            } catch (Throwable ignored) {
            }
        }
    }
}
