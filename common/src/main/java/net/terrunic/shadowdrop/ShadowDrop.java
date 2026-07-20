package net.terrunic.shadowdrop;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.terrunic.shadowdrop.compat.EmiCompat;
import net.terrunic.shadowdrop.platform.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShadowDrop {
    public static final String MOD_ID = "shadowdrop";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Bool to force refresh of corner pixel tests for drop shadows
    public static boolean shouldRefresh = false;
    // Tracker for item currently under cursor
    public static ItemStack hoveredItem = null;
    // Tracker for current active GuiGraphics
    public static Object currentGuiGraphics = null;
    // Tracker for GUI item render depth
    public static int guiRenderDepth = 0;
    // Tracker for slot currently being rendered in container screens
    public static Slot currentRenderingSlot = null;
    public static boolean isRenderingEmiSlot = false;
    // Whether the EMI slot currently being rendered is a larger output slot
    public static boolean isRenderingEmiOutputSlot = false;
    // Bounds of the EMI slot currently being rendered
    public static int emiSlotWidth = 16;
    public static int emiSlotHeight = 16;

    public static void init() {
        ShadowDropConfig.load();

        if (Services.PLATFORM.isModLoaded("emi")) {
            EmiCompat.init();
        }
    }
}
