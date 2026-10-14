package net.terrunic.shadowdrop.mixin;

//? if <26.1 {
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.terrunic.shadowdrop.ShadowDrop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Mixin to track when toasts are rendering
@Mixin(ToastComponent.class)
public class ToastComponentMixin {
    // Track toasts start rendering
    @Inject(method = "render", at = @At("HEAD"))
    private void shadowdrop$startRenderToasts(GuiGraphics guiGraphics, CallbackInfo ci) {
        ShadowDrop.isToastRendering = true;
    }

    // Track toasts end rendering
    @Inject(method = "render", at = @At("TAIL"))
    private void shadowdrop$endRenderToasts(GuiGraphics guiGraphics, CallbackInfo ci) {
        ShadowDrop.isToastRendering = false;
    }
}
//?}
