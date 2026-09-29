package net.terrunic.shadowdrop.forge;

//? if forge {
/*import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.terrunic.shadowdrop.ShadowDrop;
import net.terrunic.shadowdrop.client.YaclIntegration;
import net.terrunic.shadowdrop.platform.Platform;

@Mod(ShadowDrop.MOD_ID)
public class ForgeShadowDrop {
    public ForgeShadowDrop() {
        if (!FMLEnvironment.dist.isClient()) return;

        ShadowDrop.init();

        if (Platform.isModLoaded("yet_another_config_lib_v3")) {
            ModLoadingContext.get().registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (minecraft, parent) -> YaclIntegration.createScreen(parent)
                    )
            );
        }
    }
}
*///?}
