package net.terrunic.shadowdrop;

import net.minecraftforge.fml.common.Mod;

@Mod(ShadowDrop.MOD_ID)
public class ShadowDrop
{
    public static final String MOD_ID = "shadowdrop";

    // Client bool to force refresh of corner pixel tests for drop shadows
    public static boolean shouldRefresh = false;

    public ShadowDrop()
    {
        ShadowDropConfig.register();
    }
}