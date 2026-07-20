package net.terrunic.shadowdrop.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;

public class VertexConsumerWrapper implements VertexConsumer {
    protected final VertexConsumer delegate;
    protected final int shadowR;
    protected final int shadowG;
    protected final int shadowB;
    protected final int shadowA;

    public VertexConsumerWrapper(VertexConsumer delegate, float r, float g, float b, float a) {
        this.delegate = delegate;
        this.shadowR = (int) (r * 255f);
        this.shadowG = (int) (g * 255f);
        this.shadowB = (int) (b * 255f);
        this.shadowA = (int) (a * 255f);
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        delegate.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        int targetR = shadowR;
        int targetG = shadowG;
        int targetB = shadowB;
        int targetA = (a * shadowA) / 255;
        delegate.setColor(targetR, targetG, targetB, targetA);
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        delegate.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        delegate.setNormal(x, y, z);
        return this;
    }

    @Override
    public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightness, float r, float g, float b, float a, int[] lightmap, int overlay, boolean readExistingColor) {
        delegate.putBulkData(pose, quad, brightness, shadowR / 255f, shadowG / 255f, shadowB / 255f, (a * shadowA / 255f), lightmap, overlay, readExistingColor);
    }

    @Override
    public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float r, float g, float b, float a, int lightmap, int overlay) {
        delegate.putBulkData(pose, quad, shadowR / 255f, shadowG / 255f, shadowB / 255f, (a * shadowA / 255f), lightmap, overlay);
    }
}
