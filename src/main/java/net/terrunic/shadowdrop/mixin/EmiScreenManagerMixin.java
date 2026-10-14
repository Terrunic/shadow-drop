package net.terrunic.shadowdrop.mixin;

//? if <26.1 {
import net.terrunic.shadowdrop.ShadowDrop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Mixin to track when EMI is rendering its sidebars, or a stack dragged out of them
@Mixin(targets = "dev.emi.emi.screen.EmiScreenManager")
public class EmiScreenManagerMixin {
    // Track sidebar start rendering
    @Inject(method = "render", at = @At("HEAD"), remap = false)
    private static void shadowdrop$startRenderSidebars(CallbackInfo ci) {
        ShadowDrop.isEmiSidebarRendering = true;
    }

    // Track sidebar end rendering
    @Inject(method = "render", at = @At("TAIL"), remap = false)
    private static void shadowdrop$endRenderSidebars(CallbackInfo ci) {
        ShadowDrop.isEmiSidebarRendering = false;
    }

    // Track dragged stack start rendering
    @Inject(method = "renderDraggedStack", at = @At("HEAD"), remap = false)
    private static void shadowdrop$startRenderDraggedStack(CallbackInfo ci) {
        ShadowDrop.isEmiDragRendering = true;
    }

    // Track dragged stack end rendering
    @Inject(method = "renderDraggedStack", at = @At("TAIL"), remap = false)
    private static void shadowdrop$endRenderDraggedStack(CallbackInfo ci) {
        ShadowDrop.isEmiDragRendering = false;
    }
}
//?}
