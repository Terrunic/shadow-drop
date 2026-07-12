package com.evandev.shadowdrop.render;

import com.evandev.shadowdrop.platform.Services;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.Optional;

public class ShadowBufferSource implements MultiBufferSource {
    private final MultiBufferSource delegate;
    private final float r, g, b, a;
    private RenderType lastShadowType = null;

    public ShadowBufferSource(MultiBufferSource delegate, float r, float g, float b, float a) {
        this.delegate = delegate;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public RenderType getLastShadowType() {
        return lastShadowType;
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        if (type.name.contains("glint")) {
            return DummyVertexConsumer.INSTANCE;
        }

        ResourceLocation texture = InventoryMenu.BLOCK_ATLAS;
        if (type instanceof RenderType.CompositeRenderType compositeType) {
            RenderType.CompositeState state = compositeType.state;
            Optional<ResourceLocation> optTexture = state.textureState.cutoutTexture();
            if (optTexture.isPresent()) {
                texture = optTexture.get();
            }
        }
        RenderType targetType = RenderType.entityTranslucentCull(texture);
        lastShadowType = targetType;

        return Services.PLATFORM.wrapVertexConsumer(delegate.getBuffer(targetType), r, g, b, a);
    }
}
