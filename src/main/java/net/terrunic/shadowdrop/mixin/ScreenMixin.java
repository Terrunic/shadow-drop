package net.terrunic.shadowdrop.mixin;

//? if <26.1 {
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.terrunic.shadowdrop.ShadowDrop;
import net.terrunic.shadowdrop.ShadowDropConfig;
import net.terrunic.shadowdrop.mixin.accessor.AbstractContainerScreenAccessor;
import net.terrunic.shadowdrop.util.PixelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Mixin to track screen changes and hovered item
@Mixin(Screen.class)
public class ScreenMixin {
    // Track new screen init to refresh shadows
    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("HEAD"))
    private void shadowdrop$onInit(CallbackInfo ci) {
        ShadowDrop.shouldRefresh = true;
        PixelReader.invalidateCapture();
    }

    // Track current GUI pixels on initial render (before elements added)
    @Inject(method = "renderBackground", at = @At("TAIL"))
            //? if >=1.21 {
    public void shadowdrop$onRenderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        //?} else {
        /*public void shadowdrop$onRenderBackground(GuiGraphics guiGraphics, CallbackInfo ci) {
         *///?}
        if (PixelReader.hasCapture() || !ShadowDropConfig.CLIENT.modEnabled) return;

        Window window = Minecraft.getInstance().getWindow();
        PixelReader.capture(window.getWidth(), window.getHeight());
    }

    // Track hovered item
    @Inject(method = "render", at = @At("HEAD"))
    private void shadowdrop$onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            Slot hoveredSlot = ((AbstractContainerScreenAccessor) containerScreen).getHoveredSlot();
            ShadowDrop.hoveredItem = hoveredSlot != null ? hoveredSlot.getItem() : ItemStack.EMPTY;
        } else {
            ShadowDrop.hoveredItem = null;
        }
        ShadowDrop.hoveredItemRendered = false;
    }
}
//?}
