package net.terrunic.shadowdrop.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.resources.ResourceLocation;
import net.terrunic.shadowdrop.ShadowDrop;

public class FabricShaders {
    public static void register() {
        CoreShaderRegistrationCallback.EVENT.register(context -> {
            context.register(
                    ResourceLocation.fromNamespaceAndPath(ShadowDrop.MOD_ID, "rendertype_shadow"),
                    DefaultVertexFormat.NEW_ENTITY,
                    shader -> ShadowDropShaders.shadowShader = shader
            );
        });
    }
}
