package net.terrunic.shadowdrop.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.resources.ResourceLocation;

// Fabric custom shaders registry
public class FabricShaders {
    public static void register() {
        CoreShaderRegistrationCallback.EVENT.register(context -> {
            context.register(
                new ResourceLocation("shadowdrop", "rendertype_shadow"),
                DefaultVertexFormat.NEW_ENTITY,
                shader -> ShadowDropShaders.shadowShader = shader
            );
        });
    }
}