package net.terrunic.shadowdrop.mixin;

//? if >=26.1 {
/*import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.terrunic.shadowdrop.render.ShadowItemState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.3 {
/^import com.mojang.renderpearl.api.textures.FilterMode;
^///?} else {
import com.mojang.blaze3d.textures.FilterMode;
//?}

// Mixin to draw shadows from the item atlas before the items themselves
@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
    @Shadow
    @Final
    private GuiRenderState renderState;
    @Shadow
    private GuiItemAtlas itemAtlas;

    @Inject(method = "prepareItemElements", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/state/gui/GuiRenderState;forEachItem(Ljava/util/function/Consumer;)V", ordinal = 0))
    private void shadowdrop$addShadows(CallbackInfo ci) {
        if (this.itemAtlas == null) return;
        this.renderState.forEachItem(this::shadowdrop$addShadow);
    }

    @Unique
    private void shadowdrop$addShadow(GuiItemRenderState itemState) {
        // Oversized items render outside the atlas
        if (itemState.oversizedItemBounds() != null) return;

        ShadowItemState shadowState = (ShadowItemState) (Object) itemState;
        if (!shadowState.shadowdrop$hasShadow()) return;

        GuiItemAtlas.SlotView slotView = this.itemAtlas.getOrUpdate(itemState.itemStackRenderState());
        if (slotView == null) return;

        int itemX = itemState.x();
        int itemY = itemState.y();
        int x0 = itemX + shadowState.shadowdrop$getXOffset();
        int y0 = itemY + shadowState.shadowdrop$getYOffset();
        int x1 = x0 + 16;
        int y1 = y0 + 16;

        float u0 = slotView.u0();
        float u1 = slotView.u1();
        float v0 = slotView.v0();
        float v1 = slotView.v1();

        // Crop to the item's own 16x16 square, trimming the atlas region to match
        if (shadowState.shadowdrop$isCropped()) {
            int cx0 = Math.max(x0, itemX);
            int cy0 = Math.max(y0, itemY);
            int cx1 = Math.min(x1, itemX + 16);
            int cy1 = Math.min(y1, itemY + 16);
            if (cx0 >= cx1 || cy0 >= cy1) return;

            float du = (u1 - u0) / 16f;
            float dv = (v1 - v0) / 16f;
            float cu0 = u0 + (cx0 - x0) * du;
            float cu1 = u0 + (cx1 - x0) * du;
            float cv0 = v0 + (cy0 - y0) * dv;
            float cv1 = v0 + (cy1 - y0) * dv;

            x0 = cx0;
            y0 = cy0;
            x1 = cx1;
            y1 = cy1;
            u0 = cu0;
            u1 = cu1;
            v0 = cv0;
            v1 = cv1;
        }

        this.renderState.addBlitToCurrentLayer(new BlitRenderState(
                RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
                TextureSetup.singleTexture(slotView.textureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)),
                itemState.pose(),
                x0, y0, x1, y1,
                u0, u1, v0, v1,
                shadowState.shadowdrop$getColor(),
                itemState.scissorArea()
        ));
    }
}
*///?}
