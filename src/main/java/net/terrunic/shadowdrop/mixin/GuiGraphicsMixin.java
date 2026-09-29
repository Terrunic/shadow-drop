package net.terrunic.shadowdrop.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.terrunic.shadowdrop.ShadowDrop;
import net.terrunic.shadowdrop.ShadowDropConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if >=26.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.inventory.Slot;
import net.terrunic.shadowdrop.mixin.accessor.AbstractContainerScreenAccessor;
import net.terrunic.shadowdrop.render.ShadowItemState;
import net.terrunic.shadowdrop.util.ShadowContext;
import net.terrunic.shadowdrop.util.ShadowStyle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
*///?} else {
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
//?}

// Mixin to tag queued GUI items with their shadow (26.1+), or to offset tooltips and track gui graphics
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    //? if >=26.1 {
    /*@Shadow
    @Final
    private Minecraft minecraft;

    // Attach shadow data to each item as it's queued, while its render context is still known
    @WrapOperation(method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/state/gui/GuiRenderState;addItem(Lnet/minecraft/client/renderer/state/gui/GuiItemRenderState;)V"))
    private void shadowdrop$queueShadow(GuiRenderState renderState, GuiItemRenderState itemState, Operation<Void> original, LivingEntity owner, Level level, ItemStack itemStack, int x, int y, int seed) {
        if (ShadowDropConfig.CLIENT.modEnabled && !itemStack.isEmpty() && !ShadowStyle.isTransparent(itemStack)) {
            ShadowContext shadowContext = shadowdrop$getContext(itemStack);

            boolean shouldRender = switch (shadowContext) {
                case HOTBAR -> ShadowDropConfig.CLIENT.hotbarShadows;
                case SLOT -> ShadowDropConfig.CLIENT.slotShadows;
                case HOVER -> ShadowDropConfig.CLIENT.hoverShadows;
                case CURSOR -> ShadowDropConfig.CLIENT.cursorShadows;
                case ELSEWHERE -> ShadowDropConfig.CLIENT.elsewhereShadows;
            };
            int alpha = ShadowStyle.getAlpha(itemStack);

            if (shouldRender && alpha > 0) {
                boolean isCropped = switch (shadowContext) {
                    case HOTBAR -> ShadowDropConfig.CLIENT.hotbarCropped;
                    case SLOT -> ShadowDropConfig.CLIENT.slotCropped;
                    case HOVER -> ShadowDropConfig.CLIENT.hoverCropped;
                    default -> false;
                };

                // The item atlas blits with premultiplied alpha, so premultiply the shadow color to match
                int[] rgb = ShadowStyle.getColor();
                int color = ARGB.color(alpha, rgb[0] * alpha / 255, rgb[1] * alpha / 255, rgb[2] * alpha / 255);
                ((ShadowItemState) (Object) itemState).shadowdrop$setShadow(color, ShadowDropConfig.CLIENT.shadowXOffset, ShadowDropConfig.CLIENT.shadowYOffset, isCropped);
            }
        }

        original.call(renderState, itemState);
    }

    // Work out where an item is being drawn from what's on screen
    @Unique
    private ShadowContext shadowdrop$getContext(ItemStack itemStack) {
        if (ShadowDrop.isHotbarRendering) return ShadowContext.HOTBAR;
        if (minecraft.player != null && minecraft.player.containerMenu.getCarried() == itemStack) return ShadowContext.CURSOR;

        if (minecraft.screen instanceof AbstractContainerScreen<?> containerScreen) {
            Slot hoveredSlot = ((AbstractContainerScreenAccessor) containerScreen).getHoveredSlot();
            if (hoveredSlot != null && hoveredSlot.getItem() == itemStack) return ShadowContext.HOVER;

            // Slots hand out their own stack, so identity tells us the item is drawn in one
            for (Slot slot : containerScreen.getMenu().slots) {
                if (slot.getItem() == itemStack) return ShadowContext.SLOT;
            }
        }

        return ShadowContext.ELSEWHERE;
    }
    *///?} else {
    // Track current gui graphics
    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V", at = @At("HEAD"))
    private void shadowdrop$storeGuiGraphics(LivingEntity entity, Level level, ItemStack stack, int x, int y, int seed, int guiOffset, CallbackInfo ci) {
        ShadowDrop.currentGuiGraphics = this;
        ShadowDrop.guiRenderDepth = 0;
    }

    // Stop tracking current gui graphics
    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V", at = @At("RETURN"))
    private void shadowdrop$clearGuiGraphics(LivingEntity entity, Level level, ItemStack stack, int x, int y, int seed, int guiOffset, CallbackInfo ci) {
        ShadowDrop.currentGuiGraphics = null;
    }

    // Offset tooltip to ensure it remains above items
    @Inject(method = "renderTooltipInternal", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER))
    private void shadowdrop$tooltipZOffset(Font font, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, CallbackInfo ci) {
        if (ShadowDropConfig.CLIENT.modEnabled && ShadowDropConfig.CLIENT.offsetItems)
            ((GuiGraphics) (Object) this).pose().translate(0, 0, 32);
    }

    // Restore tooltip
    @Inject(method = "renderTooltipInternal", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void shadowdrop$tooltipZRestore(Font font, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, CallbackInfo ci) {
        if (ShadowDropConfig.CLIENT.modEnabled && ShadowDropConfig.CLIENT.offsetItems)
            ((GuiGraphics) (Object) this).pose().translate(0, 0, -32);
    }
    //?}
}
