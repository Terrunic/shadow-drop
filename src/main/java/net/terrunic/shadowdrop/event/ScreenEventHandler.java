package net.terrunic.shadowdrop.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.terrunic.shadowdrop.ShadowDrop;

// Event subscriber to refresh drop shadows on specific events
@Mod.EventBusSubscriber(modid = ShadowDrop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScreenEventHandler
{
    // When new screen initialises, trigger shadow refresh
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init event)
    {
        // Track screen updates for refreshing shadows
        ShadowDrop.shouldRefresh = true;
    }

    // Every frame in a GUI, check for hovered item
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event)
    {
        // Track item under cursor
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof AbstractContainerScreen<?> screen) {
            ShadowDrop.hoveredItem = screen.getSlotUnderMouse() != null ? screen.getSlotUnderMouse().getItem() : ItemStack.EMPTY;
        }
        else ShadowDrop.hoveredItem = ItemStack.EMPTY;
    }
}
