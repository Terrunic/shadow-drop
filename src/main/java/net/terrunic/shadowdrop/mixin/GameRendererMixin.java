package net.terrunic.shadowdrop.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.terrunic.shadowdrop.ShadowDrop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Mixin to detect when level is rendering
@Mixin(GameRenderer.class)
public class GameRendererMixin
{
    // Track level start rendering
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void shadowdrop$startRenderLevel(CallbackInfo ci)
    {
        ShadowDrop.isLevelRendering = true;
    }

    // Track level end rendering
    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void shadowdrop$endRenderLevel(CallbackInfo ci)
    {
        ShadowDrop.isLevelRendering = false;
    }
}