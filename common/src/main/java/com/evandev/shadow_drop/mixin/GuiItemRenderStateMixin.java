package com.evandev.shadow_drop.mixin;

import com.evandev.shadow_drop.api.IShadowDropItemState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiItemRenderState.class)
public class GuiItemRenderStateMixin implements IShadowDropItemState {
    @Unique
    private boolean shadowdrop$hasShadow = false;
    @Unique
    private int[] shadowdrop$color = {0, 0, 0};
    @Unique
    private int shadowdrop$alpha = 0;
    @Unique
    private int shadowdrop$xOff = 0;
    @Unique
    private int shadowdrop$yOff = 0;
    @Unique
    private boolean shadowdrop$checkCrop = false;
    @Unique
    private boolean shadowdrop$checkSlot = false;

    @Override
    public void shadowdrop$setShadowData(int[] color, int alpha, int xOff, int yOff, boolean checkCrop, boolean checkSlot) {
        this.shadowdrop$hasShadow = true;
        this.shadowdrop$color = color;
        this.shadowdrop$alpha = alpha;
        this.shadowdrop$xOff = xOff;
        this.shadowdrop$yOff = yOff;
        this.shadowdrop$checkCrop = checkCrop;
        this.shadowdrop$checkSlot = checkSlot;
    }

    @Override
    public boolean shadowdrop$hasShadow() {
        return shadowdrop$hasShadow;
    }

    @Override
    public int[] shadowdrop$getColor() {
        return shadowdrop$color;
    }

    @Override
    public int shadowdrop$getAlpha() {
        return shadowdrop$alpha;
    }

    @Override
    public int shadowdrop$getXOff() {
        return shadowdrop$xOff;
    }

    @Override
    public int shadowdrop$getYOff() {
        return shadowdrop$yOff;
    }

    @Override
    public boolean shadowdrop$shouldCheckCrop() {
        return shadowdrop$checkCrop;
    }

    @Override
    public boolean shadowdrop$shouldCheckSlot() {
        return shadowdrop$checkSlot;
    }
}