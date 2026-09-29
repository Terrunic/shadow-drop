package net.terrunic.shadowdrop.fabric.compat;

//? if fabric {
/*import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.terrunic.shadowdrop.client.YaclIntegration;
import net.terrunic.shadowdrop.platform.Platform;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!Platform.isModLoaded("yet_another_config_lib_v3")) return parent -> null;
        return YaclIntegration::createScreen;
    }
}
*///?}
