package net.terrunic.shadowdrop.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.terrunic.shadowdrop.ShadowDrop;

import java.io.IOException;

@EventBusSubscriber(modid = ShadowDrop.MOD_ID, value = Dist.CLIENT)
public class NeoForgeShaders {
    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(ShadowDrop.MOD_ID, "rendertype_shadow"),
                        DefaultVertexFormat.NEW_ENTITY),
                shader -> ShadowDropShaders.shadowShader = shader
        );
    }
}
