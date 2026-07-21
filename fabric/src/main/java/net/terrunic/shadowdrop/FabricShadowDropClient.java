package net.terrunic.shadowdrop;

import net.terrunic.shadowdrop.shader.FabricShaders;
import net.fabricmc.api.ClientModInitializer;

public class FabricShadowDropClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricShaders.register();
    }
}