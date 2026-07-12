package com.evandev.shadowdrop;

import com.evandev.shadowdrop.client.YaclConfigIntegration;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(ShadowDrop.MOD_ID)
public class NeoForgeShadowDrop {
    public NeoForgeShadowDrop(ModContainer container) {
        ShadowDrop.init();

        if (FMLEnvironment.dist.isClient()) {
            if (ModList.get().isLoaded("yet_another_config_lib_v3")) {
                container.registerExtensionPoint(IConfigScreenFactory.class,
                        (c, parent) -> YaclConfigIntegration.createScreen(parent));
            }
        }
    }
}
