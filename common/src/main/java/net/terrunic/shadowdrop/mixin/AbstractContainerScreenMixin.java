package net.terrunic.shadowdrop.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.terrunic.shadowdrop.ShadowDrop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void shadowdrop$onRenderSlotHead(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        ShadowDrop.currentRenderingSlot = slot;
    }

    @Inject(method = "renderSlot", at = @At("RETURN"))
    private void shadowdrop$onRenderSlotReturn(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        ShadowDrop.currentRenderingSlot = null;
    }
}
