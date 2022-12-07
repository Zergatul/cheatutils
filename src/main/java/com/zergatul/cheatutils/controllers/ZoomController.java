package com.zergatul.cheatutils.controllers;

import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ZoomController {

    public static final ZoomController instance = new ZoomController();

    private boolean isZooming;
    private double fovFactor;

    private ZoomController() {
        //ModApiWrapper.RenderTickStart.add(this::onRenderTickStart);
    }

    public boolean isActive() {
        return isZooming;
    }

    public double getFovFactor() {
        return fovFactor;
    }

    @SubscribeEvent
    public void onFovComputer(ViewportEvent.ComputeFov event) {
        /*if (KeyBindingsController.instance.keys[9].isDown()) {
            isZooming = true;
            fovFactor = 10 / event.getFOV();
            event.setFOV(10);
        } else {
            isZooming = false;
        }*/
    }
}