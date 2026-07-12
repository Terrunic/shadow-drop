package net.terrunic.shadowdrop.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

// Mixin to adjust tooltips with item offset
@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin
{
    // Offset tooltip to ensure it remains above items
    @Inject(method = "renderTooltipInternal", at = @At("HEAD"))
    private void shadowdrop$tooltipZOffset(Font font, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, CallbackInfo ci)
    {
        ((GuiGraphics)(Object)this).pose().translate(0, 0, 32);
    }

    // Restore tooltip
    @Inject(method = "renderTooltipInternal", at = @At("TAIL"))
    private void shadowdrop$tooltipZRestore(Font font, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, CallbackInfo ci)
    {
        ((GuiGraphics)(Object)this).pose().translate(0, 0, -32);
    }
}
