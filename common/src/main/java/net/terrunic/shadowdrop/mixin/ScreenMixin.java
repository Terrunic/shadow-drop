package net.terrunic.shadowdrop.mixin;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.terrunic.shadowdrop.ShadowDrop;
import net.terrunic.shadowdrop.ShadowDropConfig;
import net.terrunic.shadowdrop.util.PixelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

// Mixin to track screen changes and hovered item
@Mixin(Screen.class)
public class ScreenMixin {
    // Track new screen init to refresh shadows
    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("HEAD"))
    private void shadowdrop$onInit(CallbackInfo ci) {
        ShadowDrop.shouldRefresh = true;
        ShadowDrop.guiInitRenderBuffer = null;
    }

    // Track current GUI pixels on initial render (before elements added)
    @Inject(method = "renderBackground", at = @At("TAIL"))
    public void shadowdrop$onRenderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (ShadowDrop.guiInitRenderBuffer != null || !ShadowDropConfig.CLIENT.modEnabled) return;

        Window window = Minecraft.getInstance().getWindow();
        int width = window.getWidth();
        int height = window.getHeight();
        if (width <= 0 || height <= 0) return;

        ByteBuffer buffer = PixelReader.screenBuffer(width * height * 4);
        PixelReader.read(0, 0, width, height, buffer);
        ShadowDrop.guiInitRenderBuffer = buffer;
    }

    // Track hovered item
    @Inject(method = "render", at = @At("HEAD"))
    private void shadowdrop$onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            ShadowDrop.hoveredItem = containerScreen.hoveredSlot != null ? containerScreen.hoveredSlot.getItem() : ItemStack.EMPTY;
        } else {
            ShadowDrop.hoveredItem = null;
        }
        ShadowDrop.hoveredItemRendered = false;
    }
}
