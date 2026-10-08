package net.terrunic.shadowdrop.mixin;

//? if <26.1 {
import net.minecraft.client.renderer.GameRenderer;
import net.terrunic.shadowdrop.ShadowDrop;
import net.terrunic.shadowdrop.util.PixelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Mixin to track frames and when level is rendering
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    // Track frame start
    @Inject(method = "render", at = @At("HEAD"))
    private void shadowdrop$startFrame(CallbackInfo ci) {
        PixelReader.startFrame();
    }

    // Track level start rendering
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void shadowdrop$startRenderLevel(CallbackInfo ci) {
        ShadowDrop.isLevelRendering = true;
    }

    // Track level end rendering
    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void shadowdrop$endRenderLevel(CallbackInfo ci) {
        ShadowDrop.isLevelRendering = false;
    }
}
//?}
