package net.terrunic.shadowdrop;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

// Event subscriber to refresh drop shadows on specific events
@EventBusSubscriber(modid = ShadowDrop.MOD_ID)
public class ScreenEventHandler
{
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Pre event)
    {
        ShadowDrop.shouldRefresh = true;
    }
}
