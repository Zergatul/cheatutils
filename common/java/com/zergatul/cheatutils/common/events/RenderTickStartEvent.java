package com.zergatul.cheatutils.common.events;

public class RenderTickStartEvent {

    private final float partialTicks;

    public RenderTickStartEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}