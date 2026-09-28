package net.terrunic.shadowdrop.mixin;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.terrunic.shadowdrop.ShadowDrop;
import net.terrunic.shadowdrop.ShadowDropConfig;
import net.terrunic.shadowdrop.util.PixelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.emi.emi.screen.RecipeScreen")
public class EmiRecipeScreenMixin {
    @Inject(method = "render", at = @At("HEAD"), remap = false)
    private void shadowdrop$onRender(GuiGraphics raw, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!PixelReader.hasCapture() && ShadowDropConfig.CLIENT.modEnabled) {
            Window window = Minecraft.getInstance().getWindow();
            PixelReader.capture(window.getWidth(), window.getHeight());
        }

        ShadowDrop.hoveredItem = null;
        ShadowDrop.hoveredItemRendered = false;
    }
}
