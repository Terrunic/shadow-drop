package net.terrunic.shadowdrop.mixin;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.terrunic.shadowdrop.ShadowDrop;
import net.terrunic.shadowdrop.ShadowDropConfig;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.emi.emi.screen.RecipeScreen")
public class EmiRecipeScreenMixin {
    @Inject(method = "render", at = @At("HEAD"), remap = false)
    private void shadowdrop$onRender(GuiGraphics raw, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (ShadowDrop.guiInitRenderBuffer == null && ShadowDropConfig.CLIENT.modEnabled) {
            Window window = Minecraft.getInstance().getWindow();
            int width = window.getWidth();
            int height = window.getHeight();
            ShadowDrop.guiInitRenderBuffer = BufferUtils.createByteBuffer(width * height * 4);
            GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, ShadowDrop.guiInitRenderBuffer);
        }

        ShadowDrop.hoveredItem = null;
        ShadowDrop.hoveredItemRendered = false;
    }
}
