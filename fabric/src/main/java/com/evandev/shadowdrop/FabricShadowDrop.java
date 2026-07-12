package com.evandev.shadowdrop;

import net.fabricmc.api.ModInitializer;

public class FabricShadowDrop implements ModInitializer {

    @Override
    public void onInitialize() {
        ShadowDrop.init();
    }
}
