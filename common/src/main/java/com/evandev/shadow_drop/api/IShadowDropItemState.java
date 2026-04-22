package com.evandev.shadow_drop.api;

public interface IShadowDropItemState {
    void shadowdrop$setShadowData(int[] color, int alpha, int xOff, int yOff, boolean checkCrop, boolean checkSlot);

    boolean shadowdrop$hasShadow();

    int[] shadowdrop$getColor();

    int shadowdrop$getAlpha();

    int shadowdrop$getXOff();

    int shadowdrop$getYOff();

    boolean shadowdrop$shouldCheckCrop();

    boolean shadowdrop$shouldCheckSlot();
}