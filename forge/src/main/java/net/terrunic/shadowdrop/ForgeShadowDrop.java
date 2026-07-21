package net.terrunic.shadowdrop;

import net.terrunic.shadowdrop.client.YaclIntegration;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(net.terrunic.shadowdrop.ShadowDrop.MOD_ID)
public class ForgeShadowDrop {
    public ForgeShadowDrop() {
        ShadowDrop.init();

        // Forge YACL integration
        if (FMLEnvironment.dist.isClient()) {
            if (ModList.get().isLoaded("yet_another_config_lib_v3")) {
                ModLoadingContext.get().registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> YaclIntegration.createScreen(parent)
                    )
                );
            }
        }
    }
}