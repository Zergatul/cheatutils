package com.zergatul.cheatutils.controllers;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientTickController {

    public static final ClientTickController instance = new ClientTickController();

    private final Minecraft mc = Minecraft.getInstance();
    public final Object clientTickStartEvent = new Object();
    public final Object clientTickEndEvent = new Object();
    public float partialTicks;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            synchronized (clientTickStartEvent) {
                clientTickStartEvent.notifyAll();
            }
        }
        if (event.phase == TickEvent.Phase.END) {
            partialTicks = mc.isPaused() ? 0/*mc.pausePartialTick*/ : mc.getFrameTime();
            synchronized (clientTickEndEvent) {
                clientTickEndEvent.notifyAll();
            }
        }
    }
}
