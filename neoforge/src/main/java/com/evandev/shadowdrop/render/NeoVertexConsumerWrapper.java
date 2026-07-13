package com.evandev.shadowdrop.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;

public class NeoVertexConsumerWrapper extends VertexConsumerWrapper {

    public NeoVertexConsumerWrapper(VertexConsumer delegate, float r, float g, float b, float a) {
        super(delegate, r, g, b, a);
    }

    @Override
    public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float r, float g, float b, float a, int packedLight, int packedOverlay, boolean readExistingColor) {
        delegate.putBulkData(pose, quad, shadowR / 255f, shadowG / 255f, shadowB / 255f, (a * shadowA / 255f), packedLight, packedOverlay, readExistingColor);
    }
}