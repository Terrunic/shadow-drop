package net.terrunic.shadowdrop.render;

import net.terrunic.shadowdrop.mixin.accessor.CompositeRenderTypeAccessor;
import net.terrunic.shadowdrop.mixin.accessor.CompositeStateAccessor;
import net.terrunic.shadowdrop.mixin.accessor.EmptyTextureStateShardAccessor;
import net.terrunic.shadowdrop.mixin.accessor.RenderStateShardAccessor;
import net.terrunic.shadowdrop.platform.Services;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ShadowBufferSource implements MultiBufferSource {
    private final MultiBufferSource delegate;
    private final float r, g, b, a;
    private final Set<RenderType> usedTypes = new HashSet<>();

    public ShadowBufferSource(MultiBufferSource delegate, float r, float g, float b, float a) {
        this.delegate = delegate;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void endShadowBatches(BufferSource immediate) {
        for (RenderType type : usedTypes) {
            immediate.endBatch(type);
        }
        usedTypes.clear();
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

        RenderType targetType = ShadowRenderType.get(texture);
        usedTypes.add(targetType);

        return Services.PLATFORM.wrapVertexConsumer(delegate.getBuffer(targetType), r, g, b, a);
    }
}