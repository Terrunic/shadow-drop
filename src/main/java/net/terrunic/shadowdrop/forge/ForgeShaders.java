package net.terrunic.shadowdrop.forge;

//? if forge {
/*import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.terrunic.shadowdrop.ShadowDrop;
import net.terrunic.shadowdrop.shader.ShadowDropShaders;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = ShadowDrop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ForgeShaders {
    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                        new ResourceLocation(ShadowDrop.MOD_ID, "rendertype_shadow"),
                        DefaultVertexFormat.NEW_ENTITY),
                shader -> ShadowDropShaders.shadowShader = shader
        );
    }
}
*///?}
