package com.evandev.shadowdrop;

import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import com.evandev.shadowdrop.client.YaclConfigIntegration;
import com.evandev.shadowdrop.compat.EmiCompat;

@Mod(ShadowDrop.MOD_ID)
public class ShadowDrop {
    public static final String MOD_ID = "shadowdrop";

    // Bool to force refresh of corner pixel tests for drop shadows
    public static boolean shouldRefresh = false;
    // Tracker for item currently under cursor
    public static ItemStack hoveredItem = ItemStack.EMPTY;

    public ShadowDrop(ModContainer container) {
        ShadowDropConfig.register(container);
        if (ModList.get().isLoaded("emi")) {
            try {
                EmiCompat.init();
            } catch (Throwable ignored) {
            }
        }

        if (FMLEnvironment.dist.isClient()) {
            if (ModList.get().isLoaded("yet_another_config_lib_v3")) {
                container.registerExtensionPoint(IConfigScreenFactory.class,
                        (c, parent) -> YaclConfigIntegration.createScreen(parent));
            }
        }
    }
}