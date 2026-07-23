package net.terrunic.shadowdrop.mixin;

import net.terrunic.shadowdrop.api.IShadowDropItemState;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {

    @Shadow
    @Final
    private GuiRenderState renderState;
    @Shadow
    private GuiItemAtlas itemAtlas;

    @Inject(method = "prepareItemElements",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/state/gui/GuiRenderState;forEachItem(Ljava/util/function/Consumer;)V", ordinal = 0))
    private void shadowdrop$preAddShadows(CallbackInfo ci) {
        if (this.itemAtlas == null) return;

        this.renderState.forEachItem(itemState -> {
            if (itemState.oversizedItemBounds() == null) {
                IShadowDropItemState shadowState = (IShadowDropItemState) (Object) itemState;

                if (shadowState != null && shadowState.shadowdrop$hasShadow()) {
                    GuiItemAtlas.SlotView slotView = this.itemAtlas.getOrUpdate(itemState.itemStackRenderState());

                    if (slotView != null) {
                        int[] rgb = shadowState.shadowdrop$getColor();
                        int color = ARGB.color(shadowState.shadowdrop$getAlpha(), rgb[0], rgb[1], rgb[2]);

                        int xOff = shadowState.shadowdrop$getXOff();
                        int yOff = shadowState.shadowdrop$getYOff();

                        int x1 = itemState.x() + xOff;
                        int y1 = itemState.y() + yOff;
                        int x2 = x1 + 16;
                        int y2 = y1 + 16;

                        float u0 = slotView.u0();
                        float u1 = slotView.u1();
                        float v0 = slotView.v0();
                        float v1 = slotView.v1();

                        if (shadowState.shadowdrop$shouldCheckCrop()) {
                            x2 -= xOff;
                            y2 -= yOff;
                            u1 = u0 + (u1 - u0) * ((16f - xOff) / 16f);
                            v1 = v0 + (v1 - v0) * ((16f - yOff) / 16f);
                        }

                        ScreenRectangle scissor = itemState.scissorArea();

                        this.renderState.addBlitToCurrentLayer(new BlitRenderState(
                                RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
                                TextureSetup.singleTexture(slotView.textureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)),
                                itemState.pose(),
                                x1, y1, x2, y2,
                                u0, u1, v0, v1,
                                color,
                                scissor,
                                null
                        ));
                    }
                }
            }
        });
    }
}