package net.terrunic.shadowdrop.mixin;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.terrunic.shadowdrop.ShadowDrop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlotWidget.class)
public class EmiSlotWidgetMixin {
    @Shadow
    protected boolean output;

    @Inject(method = "render", at = @At("HEAD"))
    private void shadowdrop$onRenderSlotHead(GuiGraphics draw, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ShadowDrop.isRenderingEmiSlot = true;
        ShadowDrop.isRenderingEmiOutputSlot = output;
        if (output) {
            Bounds bounds = ((SlotWidget) (Object) this).getBounds();
            ShadowDrop.emiSlotWidth = bounds.width();
            ShadowDrop.emiSlotHeight = bounds.height();
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void shadowdrop$onRenderSlotReturn(GuiGraphics draw, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ShadowDrop.isRenderingEmiSlot = false;
        ShadowDrop.isRenderingEmiOutputSlot = false;
    }
}
