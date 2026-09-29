package net.terrunic.shadowdrop.neoforge;

//? if neoforge && <26.1 {
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.terrunic.shadowdrop.ShadowDrop;
import net.terrunic.shadowdrop.shader.ShadowDropShaders;

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
//?}
