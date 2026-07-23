package net.terrunic.shadowdrop.mixin.accessor;

import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RenderType.CompositeRenderType.class)
public interface CompositeRenderTypeAccessor {
    @Accessor("state")
    RenderType.CompositeState getState();
}