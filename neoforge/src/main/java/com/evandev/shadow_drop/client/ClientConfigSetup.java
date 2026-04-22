package com.evandev.shadow_drop.client;

import com.evandev.shadow_drop.client.integration.ClothConfigIntegration;
import com.evandev.shadow_drop.platform.Services;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class ClientConfigSetup {
    public static void register(ModContainer container) {
        if (Services.PLATFORM.isModLoaded("cloth_config")) {
            container.registerExtensionPoint(IConfigScreenFactory.class, (c, parent) -> ClothConfigIntegration.createScreen(parent));
        }
    }
}