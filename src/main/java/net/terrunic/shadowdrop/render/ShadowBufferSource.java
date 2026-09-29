package net.terrunic.shadowdrop.render;

//? if <26.1 {
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.terrunic.shadowdrop.mixin.accessor.CompositeRenderTypeAccessor;
import net.terrunic.shadowdrop.mixin.accessor.CompositeStateAccessor;
import net.terrunic.shadowdrop.mixin.accessor.EmptyTextureStateShardAccessor;
import net.terrunic.shadowdrop.mixin.accessor.RenderStateShardAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

//? if >=1.21 {
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
//?} else {
/*import java.util.HashSet;
import java.util.Set;
*///?}

public class ShadowBufferSource implements MultiBufferSource {
    //? if >=1.21 {
    private static final Map<RenderType, ByteBufferBuilder> BYTE_BUFFERS = new HashMap<>();
    private final Map<RenderType, BufferBuilder> startedBuilders = new LinkedHashMap<>();
    //?} else {
    /*private final Set<RenderType> usedTypes = new HashSet<>();
     *///?}

    private final MultiBufferSource delegate;
    private final float r, g, b, a;

    public ShadowBufferSource(MultiBufferSource delegate, float r, float g, float b, float a) {
        this.delegate = delegate;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    // Draw any shadow geometry buffered so far, using immediate where shadows were routed through it
    public void endShadowBatches(@Nullable BufferSource immediate) {
        //? if >=1.21 {
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
        //?} else {
        /*if (immediate != null) {
            for (RenderType type : usedTypes) {
                immediate.endBatch(type);
            }
        }
        usedTypes.clear();
        *///?}
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        String typeName = ((RenderStateShardAccessor) type).getName();
        if (typeName.contains("glint")) {
            return DummyVertexConsumer.INSTANCE;
        }

        // Skip if vertex format is mismatched (fixes items with custom renderers)
        if (!type.format().equals(ShadowRenderType.FORMAT)) {
            return delegate.getBuffer(type);
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

        //? if >=1.21 {
        if (!(delegate instanceof MultiBufferSource.BufferSource)) {
            return new VertexConsumerWrapper(delegate.getBuffer(targetType), r, g, b, a);
        }

        BufferBuilder builder = startedBuilders.computeIfAbsent(targetType, t -> new BufferBuilder(
                BYTE_BUFFERS.computeIfAbsent(t, k -> new ByteBufferBuilder(k.bufferSize())), t.mode(), t.format()));

        return new VertexConsumerWrapper(builder, r, g, b, a);
        //?} else {
        /*usedTypes.add(targetType);
        return new VertexConsumerWrapper(delegate.getBuffer(targetType), r, g, b, a);
        *///?}
    }
}
//?}
