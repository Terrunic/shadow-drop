package com.evandev.shadowdrop.mixin;

import com.evandev.shadowdrop.ShadowDrop;
import com.evandev.shadowdrop.mixin.accessor.AbstractContainerScreenAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("HEAD"))
    private void shadowdrop$onInit(CallbackInfo ci) {
        ShadowDrop.shouldRefresh = true;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void shadowdrop$onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            Slot hoveredSlot = ((AbstractContainerScreenAccessor) containerScreen).getHoveredSlot();
            ShadowDrop.hoveredItem = hoveredSlot != null ? hoveredSlot.getItem() : ItemStack.EMPTY;
        } else {
            ShadowDrop.hoveredItem = ItemStack.EMPTY;
        }
    }
}