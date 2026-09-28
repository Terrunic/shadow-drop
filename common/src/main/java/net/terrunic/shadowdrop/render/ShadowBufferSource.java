package net.terrunic.shadowdrop.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.terrunic.shadowdrop.platform.Services;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ShadowBufferSource implements MultiBufferSource {
    private static final Map<RenderType, ByteBufferBuilder> BYTE_BUFFERS = new HashMap<>();

    private final MultiBufferSource delegate;
    private final float r, g, b, a;
    private final Map<RenderType, BufferBuilder> startedBuilders = new LinkedHashMap<>();

    public ShadowBufferSource(MultiBufferSource delegate, float r, float g, float b, float a) {
        this.delegate = delegate;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void endShadowBatches() {
        for (Map.Entry<RenderType, BufferBuilder> entry : startedBuilders.entrySet()) {
            RenderType type = entry.getKey();
            MeshData meshData = entry.getValue().build();
            if (meshData == null) continue;
            if (type.sortOnUpload()) {
                meshData.sortQuads(BYTE_BUFFERS.get(type), RenderSystem.getVertexSorting());
            }
            type.draw(meshData);
        }
        startedBuilders.clear();
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        if (type.name.contains("glint")) {
            return DummyVertexConsumer.INSTANCE;
        }

        // Skip if vertex format is mismatched (fixes items with custom renderers)
        if (!type.format().equals(ShadowRenderType.FORMAT)) {
            return delegate.getBuffer(type);
        }

        ResourceLocation texture = InventoryMenu.BLOCK_ATLAS;
        if (type instanceof RenderType.CompositeRenderType compositeType) {
            RenderType.CompositeState state = compositeType.state;
            Optional<ResourceLocation> optTexture = state.textureState.cutoutTexture();
            if (optTexture.isPresent()) {
                texture = optTexture.get();
            }
        }
        RenderType targetType = ShadowRenderType.get(texture);

        if (!(delegate instanceof MultiBufferSource.BufferSource)) {
            return Services.PLATFORM.wrapVertexConsumer(delegate.getBuffer(targetType), r, g, b, a);
        }

        BufferBuilder builder = startedBuilders.computeIfAbsent(targetType, t -> new BufferBuilder(
                BYTE_BUFFERS.computeIfAbsent(t, k -> new ByteBufferBuilder(k.bufferSize())), t.mode(), t.format()));

        return Services.PLATFORM.wrapVertexConsumer(builder, r, g, b, a);
    }
}
