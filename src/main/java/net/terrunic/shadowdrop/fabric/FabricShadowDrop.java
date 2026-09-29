package net.terrunic.shadowdrop.fabric;

//? if fabric {
/*import net.fabricmc.api.ClientModInitializer;
import net.terrunic.shadowdrop.ShadowDrop;

public class FabricShadowDrop implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ShadowDrop.init();

        //? if <26.1 {
        FabricShaders.register();
        //?}
    }
}
*///?}
