package com.zergatul.cheatutils.forge;

import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ForgeEvents {
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            FreeCam.instance.onClientTickStart();
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            FreeCam.instance.onRenderTickStart(event.renderTickTime);
        }
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        if (!FreeCam.instance.shouldRenderHands()) {
            event.setCanceled(true);
        }
    }
}
