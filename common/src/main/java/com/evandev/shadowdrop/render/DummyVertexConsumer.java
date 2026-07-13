package com.evandev.shadowdrop.render;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class DummyVertexConsumer implements VertexConsumer {
    public static final DummyVertexConsumer INSTANCE = new DummyVertexConsumer();

    private DummyVertexConsumer() {
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        return this;
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int i, int i1) {
        return null;
    }

    @Override
    public VertexConsumer uv2(int i, int i1) {
        return null;
    }

    @Override
    public VertexConsumer normal(float v, float v1, float v2) {
        return null;
    }

    @Override
    public void endVertex() {
    }

    @Override
    public void defaultColor(int i, int i1, int i2, int i3) {
    }

    @Override
    public void unsetDefaultColor() {
    }
}
