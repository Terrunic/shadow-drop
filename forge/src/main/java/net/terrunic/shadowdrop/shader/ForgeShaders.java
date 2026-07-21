package net.terrunic.shadowdrop.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

// Forge custom shaders registry via event
@Mod.EventBusSubscriber(modid = "shadowdrop", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ForgeShaders
{
    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException
    {
        event.registerShader(
            new ShaderInstance(event.getResourceProvider(),
                new ResourceLocation("shadowdrop", "rendertype_shadow"),
                DefaultVertexFormat.NEW_ENTITY),
            shader -> ShadowDropShaders.shadowShader = shader
        );
    }
}