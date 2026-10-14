package net.terrunic.shadowdrop.mixin;

//? if <26.1 {
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenRenderMixin {
    //? if >=1.21 {
    // Track current GUI pixels behind the container background
    @Inject(method = "renderBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V"))
    private void shadowdrop$onRenderBg(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (PixelReader.hasBackgroundCapture() || !ShadowDropConfig.CLIENT.modEnabled) return;

        Window window = Minecraft.getInstance().getWindow();
        PixelReader.capture(window.getWidth(), window.getHeight());
    }
    //?}

    @Inject(method = "render", at = @At("HEAD"))
    private void shadowdrop$onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        // Fallback for screens that skip the background, replaced once the background is captured
        if (!PixelReader.hasCapture() && ShadowDropConfig.CLIENT.modEnabled) {
            Window window = Minecraft.getInstance().getWindow();
            PixelReader.captureFallback(window.getWidth(), window.getHeight());
        }

        Slot hoveredSlot = ((AbstractContainerScreenAccessor) this).getHoveredSlot();
        ShadowDrop.hoveredItem = hoveredSlot != null ? hoveredSlot.getItem() : ItemStack.EMPTY;
        ShadowDrop.hoveredItemRendered = false;
    }
}
//?}
