package net.terrunic.shadowdrop.mixin;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.terrunic.shadowdrop.ShadowDrop;
import net.terrunic.shadowdrop.ShadowDropConfig;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenRenderMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void shadowdrop$onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (ShadowDrop.guiInitRenderBuffer == null && ShadowDropConfig.CLIENT.modEnabled) {
            Window window = Minecraft.getInstance().getWindow();
            int width = window.getWidth();
            int height = window.getHeight();
            ShadowDrop.guiInitRenderBuffer = BufferUtils.createByteBuffer(width * height * 4);
            GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, ShadowDrop.guiInitRenderBuffer);
        }

        AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) (Object) this;
        ShadowDrop.hoveredItem = containerScreen.hoveredSlot != null ? containerScreen.hoveredSlot.getItem() : ItemStack.EMPTY;
        ShadowDrop.hoveredItemRendered = false;
    }
}
