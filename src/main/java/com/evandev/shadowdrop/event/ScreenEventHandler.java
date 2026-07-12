package com.evandev.shadowdrop.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import com.evandev.shadowdrop.ShadowDrop;

// Event subscriber to refresh drop shadows on specific events
@EventBusSubscriber(modid = ShadowDrop.MOD_ID)
public class ScreenEventHandler {
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Pre event) {
        // Track screen updates for refreshing shadows
        ShadowDrop.shouldRefresh = true;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        // Track item under cursor
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof AbstractContainerScreen<?> screen) {
            ShadowDrop.hoveredItem = screen.getSlotUnderMouse() != null ? screen.getSlotUnderMouse().getItem() : ItemStack.EMPTY;
        } else ShadowDrop.hoveredItem = ItemStack.EMPTY;
    }
}
