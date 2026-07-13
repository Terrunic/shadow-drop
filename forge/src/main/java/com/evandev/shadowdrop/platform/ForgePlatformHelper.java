package com.evandev.shadowdrop.platform;

import com.evandev.shadowdrop.platform.services.IPlatformHelper;
import com.evandev.shadowdrop.render.ForgeVertexConsumerWrapper;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isPhysicalClient() {
        return FMLLoader.getDist() == Dist.CLIENT;
    }

    @Override
    public VertexConsumer wrapVertexConsumer(VertexConsumer delegate, float r, float g, float b, float a) {
        return new ForgeVertexConsumerWrapper(delegate, r, g, b, a);
    }
}
