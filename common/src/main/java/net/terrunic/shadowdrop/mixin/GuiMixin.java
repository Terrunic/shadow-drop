package net.terrunic.shadowdrop.mixin;

import net.terrunic.shadowdrop.ShadowDrop;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Mixin to track when hotbar is rendering
@Mixin(Gui.class)
public class GuiMixin {
    // Track hotbar start rendering
    @Inject(method = "renderHotbar", at = @At("HEAD"))
    private void shadowdrop$startRenderHotbar(float partialTick, GuiGraphics guiGraphics, CallbackInfo ci) {
        ShadowDrop.isHotbarRendering = true;
    }

    // Track hotbar end rendering
    @Inject(method = "renderHotbar", at = @At("TAIL"))
    private void shadowdrop$endRenderHotbar(float partialTick, GuiGraphics guiGraphics, CallbackInfo ci) {
        ShadowDrop.isHotbarRendering = false;
    }
}
