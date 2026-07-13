package com.evandev.shadowdrop.render;

import com.evandev.shadowdrop.mixin.accessor.CompositeRenderTypeAccessor;
import com.evandev.shadowdrop.mixin.accessor.CompositeStateAccessor;
import com.evandev.shadowdrop.mixin.accessor.EmptyTextureStateShardAccessor;
import com.evandev.shadowdrop.mixin.accessor.RenderStateShardAccessor;
import com.evandev.shadowdrop.platform.Services;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
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
        String typeName = ((RenderStateShardAccessor) type).getName();
        if (typeName.contains("glint")) {
            return DummyVertexConsumer.INSTANCE;
        }

        ResourceLocation texture = InventoryMenu.BLOCK_ATLAS;
        if (type instanceof CompositeRenderTypeAccessor compositeType) {
            RenderType.CompositeState state = compositeType.getState();
            RenderStateShard.EmptyTextureStateShard textureState = ((CompositeStateAccessor) (Object) state).getTextureState();

            Optional<ResourceLocation> optTexture = ((EmptyTextureStateShardAccessor) textureState).invokeCutoutTexture();
            if (optTexture.isPresent()) {
                texture = optTexture.get();
            }
        }

        RenderType targetType = RenderType.entityTranslucentCull(texture);
        lastShadowType = targetType;

        return Services.PLATFORM.wrapVertexConsumer(delegate.getBuffer(targetType), r, g, b, a);
    }
}