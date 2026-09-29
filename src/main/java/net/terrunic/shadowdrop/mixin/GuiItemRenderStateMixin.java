package net.terrunic.shadowdrop.mixin;

//? if >=26.1 {
/*import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.terrunic.shadowdrop.render.ShadowItemState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

// Mixin to store shadow data on queued GUI items
@Mixin(GuiItemRenderState.class)
public class GuiItemRenderStateMixin implements ShadowItemState {
    @Unique
    private boolean shadowdrop$hasShadow = false;
    @Unique
    private int shadowdrop$color = 0;
    @Unique
    private int shadowdrop$xOffset = 0;
    @Unique
    private int shadowdrop$yOffset = 0;
    @Unique
    private boolean shadowdrop$cropped = false;

    @Override
    public void shadowdrop$setShadow(int color, int xOffset, int yOffset, boolean cropped) {
        this.shadowdrop$hasShadow = true;
        this.shadowdrop$color = color;
        this.shadowdrop$xOffset = xOffset;
        this.shadowdrop$yOffset = yOffset;
        this.shadowdrop$cropped = cropped;
    }

    @Override
    public boolean shadowdrop$hasShadow() {
        return shadowdrop$hasShadow;
    }

    @Override
    public int shadowdrop$getColor() {
        return shadowdrop$color;
    }

    @Override
    public int shadowdrop$getXOffset() {
        return shadowdrop$xOffset;
    }

    @Override
    public int shadowdrop$getYOffset() {
        return shadowdrop$yOffset;
    }

    @Override
    public boolean shadowdrop$isCropped() {
        return shadowdrop$cropped;
    }
}
*///?}
