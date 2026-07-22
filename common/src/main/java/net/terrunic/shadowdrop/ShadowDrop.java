package net.terrunic.shadowdrop;

import net.terrunic.shadowdrop.compat.EmiCompat;
import net.terrunic.shadowdrop.platform.Services;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class ShadowDrop {
    public static final String MOD_ID = "shadowdrop";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Bool to force refresh of corner pixel tests for drop shadows
    public static boolean shouldRefresh = false;
    // Tracker for item currently under cursor
    public static ItemStack hoveredItem = null;
    // Tracker for if currently hovered item has already been rendered this frame
    public static boolean hoveredItemRendered = false;
    // Tracker for if level is rendering instead of a GUI
    public static boolean isLevelRendering = false;
    // Tracker for if hotbar is rendering instead of a GUI
    public static boolean isHotbarRendering = false;
    // Tracker for current active GuiGraphics
    public static Object currentGuiGraphics = null;
    // Tracker for current GUI render depth
    public static int guiRenderDepth = 0;
    // Tracker for current GUI pixels on initial render (before elements added)
    public static ByteBuffer guiInitRenderBuffer = null;

    public static void init() {
        ShadowDropConfig.load();

        if (Services.PLATFORM.isModLoaded("emi")) {
            EmiCompat.init();
        }
    }
}
