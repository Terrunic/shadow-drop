package net.terrunic.shadowdrop;

import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Event subscriber to refresh drop shadows on specific events
@Mod.EventBusSubscriber(modid = ShadowDrop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScreenEventHandler
{
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init event)
    {
        ShadowDrop.shouldRefresh = true;
    }
}
