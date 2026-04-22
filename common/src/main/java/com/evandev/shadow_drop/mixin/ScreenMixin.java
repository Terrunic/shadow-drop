package com.evandev.shadow_drop.mixin;

import com.evandev.shadow_drop.CommonClass;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "init()V", at = @At("HEAD"))
    private void onScreenInit(CallbackInfo ci) {
        CommonClass.shouldRefresh = true;
    }
}