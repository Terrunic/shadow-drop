package com.evandev.shadowdrop;

import com.evandev.shadowdrop.client.YaclConfigIntegration;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(ShadowDrop.MOD_ID)
public class ForgeShadowDrop {
    public ForgeShadowDrop() {
        ShadowDrop.init();

        if (FMLEnvironment.dist.isClient()) {
            if (ModList.get().isLoaded("yet_another_config_lib_v3")) {
                ModLoadingContext.get().registerExtensionPoint(
                        ConfigScreenHandler.ConfigScreenFactory.class,
                        () -> new ConfigScreenHandler.ConfigScreenFactory(
                                (minecraft, parent) -> YaclConfigIntegration.createScreen(parent)
                        )
                );
            }
        }
    }
}