package net.terrunic.shadowdrop.neoforge;

//? if neoforge {
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.terrunic.shadowdrop.ShadowDrop;
import net.terrunic.shadowdrop.client.YaclIntegration;
import net.terrunic.shadowdrop.platform.Platform;

@Mod(value = ShadowDrop.MOD_ID, dist = Dist.CLIENT)
public class NeoForgeShadowDrop {
    public NeoForgeShadowDrop(ModContainer container) {
        ShadowDrop.init();

        if (Platform.isModLoaded("yet_another_config_lib_v3")) {
            container.registerExtensionPoint(IConfigScreenFactory.class,
                    (c, parent) -> YaclIntegration.createScreen(parent));
        }
    }
}
//?}
