package net.terrunic.shadowdrop.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.terrunic.shadowdrop.platform.Services;

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
        RenderType targetType = ShadowRenderType.get(texture);
        usedTypes.add(targetType);

        return Services.PLATFORM.wrapVertexConsumer(delegate.getBuffer(targetType), r, g, b, a);
    }
}
