package net.terrunic.shadowdrop.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.terrunic.shadowdrop.ShadowDrop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.2 {
/*import net.minecraft.client.gui.Hud;
*///?} else {
import net.minecraft.client.gui.Gui;
//?}
//? if >=1.21 {
import net.minecraft.client.DeltaTracker;
//?}

// Mixin to track when hotbar is rendering
//? if >=26.2 {
/*@Mixin(Hud.class)
 *///?} else {
@Mixin(Gui.class)
//?}
public class GuiMixin {
    //? if >=26.1 {
    /*@Inject(method = "extractItemHotbar", at = @At("HEAD"))
    private void shadowdrop$startRenderHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ShadowDrop.isHotbarRendering = true;
    }

    @Inject(method = "extractItemHotbar", at = @At("TAIL"))
    private void shadowdrop$endRenderHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ShadowDrop.isHotbarRendering = false;
    }
    *///?} else if >=1.21 {
    // Track hotbar start rendering
    @Inject(method = "renderItemHotbar", at = @At("HEAD"))
    private void shadowdrop$startRenderHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ShadowDrop.isHotbarRendering = true;
    }

    // Track hotbar end rendering
    @Inject(method = "renderItemHotbar", at = @At("TAIL"))
    private void shadowdrop$endRenderHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ShadowDrop.isHotbarRendering = false;
    }
    //?} else {
    /*// Track hotbar start rendering
    @Inject(method = "renderHotbar", at = @At("HEAD"))
    private void shadowdrop$startRenderHotbar(float partialTick, GuiGraphics guiGraphics, CallbackInfo ci) {
        ShadowDrop.isHotbarRendering = true;
    }

    // Track hotbar end rendering
    @Inject(method = "renderHotbar", at = @At("TAIL"))
    private void shadowdrop$endRenderHotbar(float partialTick, GuiGraphics guiGraphics, CallbackInfo ci) {
        ShadowDrop.isHotbarRendering = false;
    }
    *///?}
}
