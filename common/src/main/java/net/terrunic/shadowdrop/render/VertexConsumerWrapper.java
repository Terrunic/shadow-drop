package net.terrunic.shadowdrop.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;

// Shadow vertex consumer wrapper
public class VertexConsumerWrapper implements VertexConsumer {
    protected final VertexConsumer delegate;
    protected final int shadowR;
    protected final int shadowG;
    protected final int shadowB;
    protected final int shadowA;

    private boolean hasUv;
    private boolean hasOverlay;
    private boolean hasLight;
    private boolean hasNormal;

    public VertexConsumerWrapper(VertexConsumer delegate, float r, float g, float b, float a) {
        this.delegate = delegate;
        this.shadowR = (int) (r * 255f);
        this.shadowG = (int) (g * 255f);
        this.shadowB = (int) (b * 255f);
        this.shadowA = (int) (a * 255f);
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        delegate.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        int targetR = shadowR;
        int targetG = shadowG;
        int targetB = shadowB;
        int targetA = (a * shadowA) / 255;
        delegate.color(targetR, targetG, targetB, targetA);
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        delegate.uv(u, v);
        hasUv = true;
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        delegate.overlayCoords(u, v);
        hasOverlay = true;
        return this;
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        delegate.uv2(u, v);
        hasLight = true;
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        delegate.normal(x, y, z);
        hasNormal = true;
        return this;
    }

    @Override
    public void endVertex() {
        if (!hasUv) {
            delegate.uv(0f, 0f);
        }
        if (!hasOverlay) {
            delegate.overlayCoords(OverlayTexture.NO_OVERLAY);
        }
        if (!hasLight) {
            delegate.uv2(LightTexture.FULL_BRIGHT);
        }
        if (!hasNormal) {
            delegate.normal(0f, 1f, 0f);
        }
        hasUv = false;
        hasOverlay = false;
        hasLight = false;
        hasNormal = false;

        delegate.endVertex();
    }

    @Override
    public void defaultColor(int r, int g, int b, int a) {
        delegate.defaultColor(r, g, b, a);
    }

    @Override
    public void unsetDefaultColor() {
        delegate.unsetDefaultColor();
    }
}