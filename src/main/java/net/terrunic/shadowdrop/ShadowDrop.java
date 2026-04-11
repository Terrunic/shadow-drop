package net.terrunic.shadowdrop;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;

@Mod(ShadowDrop.MOD_ID)
public class ShadowDrop
{
    public static final String MOD_ID = "shadowdrop";

    // Bool to force refresh of corner pixel tests for drop shadows
    public static boolean shouldRefresh = false;
    // Tracker for item currently under cursor
    public static ItemStack hoveredItem = ItemStack.EMPTY;

    public ShadowDrop()
    {
        ShadowDropConfig.register();
    }
}