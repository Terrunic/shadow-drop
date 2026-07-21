package net.terrunic.shadowdrop.compat;

import net.terrunic.shadowdrop.client.YaclIntegration;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

// Fabric YACL integration
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return YaclIntegration::createScreen;
    }
}
