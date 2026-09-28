package net.terrunic.shadowdrop.mixin;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.terrunic.shadowdrop.ShadowDrop;
import net.terrunic.shadowdrop.ShadowDropConfig;
import net.terrunic.shadowdrop.util.PixelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenRenderMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void shadowdrop$onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!PixelReader.hasCapture() && ShadowDropConfig.CLIENT.modEnabled) {
            Window window = Minecraft.getInstance().getWindow();
            PixelReader.capture(window.getWidth(), window.getHeight());
        }

        AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) (Object) this;
        ShadowDrop.hoveredItem = containerScreen.hoveredSlot != null ? containerScreen.hoveredSlot.getItem() : ItemStack.EMPTY;
        ShadowDrop.hoveredItemRendered = false;
    }
}
