package net.terrunic.shadowdrop.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.function.Function;

/*
 * Copy of entityTranslucentCull with a strict < depth test
 */
public class ShadowRenderType extends RenderType {
    public static final VertexFormat FORMAT = DefaultVertexFormat.NEW_ENTITY;
    private static final DepthTestStateShard LESS_DEPTH_TEST = new DepthTestStateShard("<", GL11.GL_LESS);

    private static final Function<ResourceLocation, RenderType> SHADOW = Util.memoize(texture -> create(
            "shadowdrop_shadow",
            FORMAT,
            VertexFormat.Mode.QUADS,
            1536,
            true,
            true,
            CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setDepthTestState(LESS_DEPTH_TEST)
                    .createCompositeState(true)));

    private ShadowRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType get(ResourceLocation texture) {
        return SHADOW.apply(texture);
    }
}
