package com.evandev.shadow_drop;

import com.evandev.shadow_drop.client.ClientConfigSetup;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Constants.MOD_ID)
public class ShadowDrop {
    public ShadowDrop(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        if (FMLEnvironment.getDist().isClient()) {
            ClientConfigSetup.register(modContainer);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        CommonClass.init();
    }
}