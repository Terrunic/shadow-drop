package net.terrunic.shadowdrop.fabric;

//? if fabric && <26.1 {
/*import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.resources.ResourceLocation;
import net.terrunic.shadowdrop.ShadowDrop;
import net.terrunic.shadowdrop.shader.ShadowDropShaders;

public class FabricShaders {
    public static void register() {
        CoreShaderRegistrationCallback.EVENT.register(context -> context.register(
                //? if >=1.21 {
                ResourceLocation.fromNamespaceAndPath(ShadowDrop.MOD_ID, "rendertype_shadow"),
                //?} else {
                /^new ResourceLocation(ShadowDrop.MOD_ID, "rendertype_shadow"),
                ^///?}
                DefaultVertexFormat.NEW_ENTITY,
                shader -> ShadowDropShaders.shadowShader = shader
        ));
    }
}
*///?}
