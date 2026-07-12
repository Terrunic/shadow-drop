package net.terrunic.shadowdrop;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.terrunic.shadowdrop.screen.ShadowConfigScreen;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.terrunic.shadowdrop.utils.ShadowContext;

@Mod(ShadowDrop.MOD_ID)
public class ShadowDrop
{
    public static final String MOD_ID = "shadowdrop";

    // Bool to force refresh of corner pixel tests for drop shadows
    public static boolean shouldRefresh = false;
    // Tracker for item currently under cursor
    public static ItemStack hoveredItem = ItemStack.EMPTY;
    // Tracker for if level is rendering
    public static boolean isLevelRendering = false;
    // Shadow context to force for next item rendered
    public static ShadowContext forcedContext = null;

    // Detected loaded mods for compat
    public static boolean installedEmi = false;
    public static boolean installedJei = false;
    public static boolean installedRei = false;

    public ShadowDrop()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ShadowDropConfig.SPEC);

        // Link config button to config screen
        ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new ShadowConfigScreen(parent)));

        // Detect loaded mods for direct compat
        if (ModList.get().isLoaded("emi")) installedEmi = true;
        if (ModList.get().isLoaded("jei")) installedJei = true;
        if (ModList.get().isLoaded("roughlyenoughitems")) installedRei = true;
    }
}