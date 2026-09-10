package com.zergatul.cheatutils.forge;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.SimpleCancellableEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ForgeEvents {

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Events.RenderTickStart.trigger(event.renderTickTime);
        }
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        if (Events.RenderHand.trigger(new SimpleCancellableEvent())) {
            event.setCanceled(true);
        }
    }
}